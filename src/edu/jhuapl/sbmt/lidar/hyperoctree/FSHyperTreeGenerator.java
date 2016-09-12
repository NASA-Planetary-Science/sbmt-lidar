package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkVertex;

import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.lidar.test.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.test.LidarPoint;
import edu.jhuapl.sbmt.model.bennu.Bennu;
import edu.jhuapl.sbmt.model.lidar.OLALidarHyperTreeSearchDataCollection;

public class OlaFSHyperTreeGenerator
{
    final Path outputDirectory;
    final int maxNumberOfPointsPerLeaf;
    final HyperBox bbox;
    final int maxNumberOfOpenOutputFiles;
    final DataOutputStreamPool pool;
    OlaFSHyperTreeNode root;
    long totalPointsWritten=0;

    BiMap<Path, Integer> fileMap=HashBiMap.create();

    public OlaFSHyperTreeGenerator(Path outputDirectory, int maxNumberOfPointsPerLeaf, HyperBox bbox, int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        this.outputDirectory=outputDirectory;
        this.maxNumberOfPointsPerLeaf=maxNumberOfPointsPerLeaf;
        this.maxNumberOfOpenOutputFiles=maxNumberOfOpenOutputFiles;
        this.bbox=bbox;
        this.pool=pool;
        root=new OlaFSHyperTreeNode(null, outputDirectory, bbox, maxNumberOfPointsPerLeaf,pool);
    }

    public void addPointsFromL2FileToRoot(Path l2FilePath, int maxPoints)
    {
        int cnt=0;
        try
        {
            String filePathString=l2FilePath.toString();
            if (!filePathString.endsWith(".l2"))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected .l2");

            int fileNum=-1;
            if (!fileMap.containsKey(l2FilePath))
            {
                fileNum=fileMap.size();
                fileMap.put(l2FilePath, fileNum);
            }
            else
                fileNum=fileMap.get(l2FilePath);

            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(l2FilePath.toFile())));

            while (true)
            {
                boolean noise = false;
                Vector3D scpos=null;
                Vector3D tgpos=null;
                double time=0;
                double intensity=0;
                double x,y,z;

                try
                {
                    in.readByte();
                }
                catch(EOFException e)
                {
                    break;
                }

                try
                {
                    // from Eli's original cube-based code
                    in.skipBytes(17 + 8 + 24);
                    time = FileUtil.readDoubleAndSwap(in);
                    in.skipBytes(8 + 2 * 3);
                    short flagStatus = MathUtil.swap(in.readShort());
                    noise = ((flagStatus == 0 || flagStatus == 1) ? false : true);
                    in.skipBytes(8 + 8 * 4);
                    x = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    y = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    z = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    tgpos=new Vector3D(x,y,z);

                    in.skipBytes(8 * 3);
                    x = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    y = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    z = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    scpos=new Vector3D(x,y,z);

                }
                catch(IOException e)
                {
                    in.close();
                    throw e;
                }

                if (!noise)
                {
                    root.add(new OlaFSHyperPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), intensity, fileNum));
                    totalPointsWritten++;
                    cnt++;
                }

                if (cnt>maxPoints)
                {
                    in.close();
                    return;
                }
            }
            in.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void expand() throws HyperException, IOException
    {
        expandNode(root);
    }

    public void expandNode(OlaFSHyperTreeNode node) throws HyperException, IOException
    {
        if (node.getNumberOfPoints()>maxNumberOfPointsPerLeaf)
        {
            node.split();
            for (int i=0; i<node.getNumberOfChildren(); i++)
                if (node.childExists(i))
                {
                    System.out.println(node.getChild(i).getPath());
                    expandNode((OlaFSHyperTreeNode)node.getChild(i));
                }
        }
    }

    public void commit() throws IOException
    {
        pool.closeAllStreams();// close any files that are still open
        finalCommit(root);
    }

    void finalCommit(OlaFSHyperTreeNode node) throws IOException
    {
        File dataFile=node.getDataFilePath().toFile();  // clean up any data files with zero points
        if (!node.isLeaf)
        {
            if (dataFile.exists())
                dataFile.delete();
            for (int i=0; i<8; i++)
                finalCommit((OlaFSHyperTreeNode)node.getChild(i));
        }
        else {
            if (!dataFile.exists() || dataFile.length()==0l)
            {
                node.getBoundsFilePath().toFile().delete();
                node.getPath().toFile().delete();
            }
        }
    }

    public double convertBytesToMB(long bytes)
    {
        return (double)bytes/(double)(1024*1024);
    }

    public long countBytes()
    {
        List<OlaFSHyperTreeNode> nodeList=getAllNonEmptyLeafNodes();
        long total=0;
        for (OlaFSHyperTreeNode node : nodeList)
            total+=node.getDataFilePath().toFile().length();
        return total;
    }

    public List<OlaFSHyperTreeNode> getAllNonEmptyLeafNodes()
    {
        List<OlaFSHyperTreeNode> nodeList=Lists.newArrayList();
        getAllNonEmptyLeafNodes(root, nodeList);
        return nodeList;
    }

    void getAllNonEmptyLeafNodes(OlaFSHyperTreeNode node, List<OlaFSHyperTreeNode> nodeList)
    {
        if (!node.isLeaf)
            for (int i=0; i<node.getNumberOfChildren(); i++)
                getAllNonEmptyLeafNodes((OlaFSHyperTreeNode)node.getChild(i), nodeList);
        else if (node.getDataFilePath().toFile().exists())
            nodeList.add(node);
    }

    public static void main(String[] args) throws HyperException, IOException
    {
//        NativeLibraryLoader.loadVtkLibraries();
//        makeTree(args);
        NativeLibraryLoader.loadVtkLibraries();
        Path outputDirectory=Paths.get("/Users/zimmemi1/Desktop/treetest/");

        double radius=1;
        double dataFileMBLimit=1;
        int dataFileByteLimit=(int)(dataFileMBLimit*1024*1024);
        int maxNumOpenOutputFiles=Integer.valueOf(32);
        int maxPointsPerLeaf=dataFileByteLimit/new OlaFSHyperPoint().getSizeInBytes();   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        DataOutputStreamPool pool=new DataOutputStreamPool(maxNumOpenOutputFiles);

        double tmin=6.0e8;
        double tmax=6.1e8;
        Configuration.setAPLVersion(true);
        ShapeModelBody body=ShapeModelBody.RQ36;
        ShapeModelAuthor author=ShapeModelAuthor.GASKELL;
        String version="V3 Image";
        Bennu bennu = new Bennu(SmallBodyViewConfig.getSmallBodyConfig(body,author,version));
        BoundingBox bbox=new BoundingBox(bennu.getBoundingBox().getBounds());
        HyperBox hbox=new HyperBox(new double[]{bbox.xmin, bbox.ymin, bbox.zmin, tmin}, new double[]{bbox.xmax, bbox.ymax, bbox.zmax, tmax});
        OlaFSHyperTreeGenerator generator=new OlaFSHyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles,pool);

        System.out.println("Adding points...");
        generator.addPointsFromL2FileToRoot(Paths.get("/Users/zimmemi1/sbmt/l2/g_0085cm_tru_obj_0000n00000_v100_00.l2"), Integer.MAX_VALUE);
        generator.addPointsFromL2FileToRoot(Paths.get("/Users/zimmemi1/sbmt/l2/g_0085cm_tru_obj_0000n00000_v100_01.l2"), Integer.MAX_VALUE);
        generator.addPointsFromL2FileToRoot(Paths.get("/Users/zimmemi1/sbmt/l2/g_0085cm_tru_obj_0000n00000_v100_02.l2"), Integer.MAX_VALUE);
        generator.addPointsFromL2FileToRoot(Paths.get("/Users/zimmemi1/sbmt/l2/g_0085cm_tru_obj_0000n00000_v100_03.l2"), Integer.MAX_VALUE);


        System.out.println("Building tree...");
        generator.expand();
        generator.commit();

        Path fileMapPath=outputDirectory.resolve("fileMap.txt");
        System.out.print("Writing file map to "+fileMapPath+"... ");
        FileWriter writer=new FileWriter(fileMapPath.toFile());
        for (int i : generator.fileMap.inverse().keySet())
            writer.write(i+" "+generator.fileMap.inverse().get(i)+"\n");
        writer.close();
        System.out.println("Done.");

        //
        //
        Path outFilePath=outputDirectory.resolve("dataSource.lidar");
        OlaFSHyperTreeCondenser condenser=new OlaFSHyperTreeCondenser(outputDirectory,outFilePath);
        condenser.condense();

        //
        //
        OlaFSHyperTreeSkeleton skeleton=new OlaFSHyperTreeSkeleton(outFilePath);
        skeleton.read();
        TreeSet<Integer> leafIds=skeleton.getLeavesIntersectingBoundingBox(hbox.getBounds());
        List<LidarPoint> points=Lists.newArrayList();
        Iterator<Integer> idIterator=leafIds.iterator();
        while (idIterator.hasNext())
        {
            int id=idIterator.next();
            Path leafPath=skeleton.getNodeById(id).getPath();
            File dataFilePath=leafPath.resolve("data").toFile();
            System.out.println("Querying leaf: "+dataFilePath.toString());
            points.addAll(OLALidarHyperTreeSearchDataCollection.readDataFile(dataFilePath, null, new double[]{0,Double.POSITIVE_INFINITY}));
        }

        System.out.println("Creating polydata...");
        vtkPoints pts=new vtkPoints();
        vtkCellArray cells=new vtkCellArray();
        vtkDoubleArray radiusArray=new vtkDoubleArray();
        for (int i=0; i<points.size(); i++)
        {
            Vector3D vec=points.get(i).getTargetPosition();
            int id=pts.InsertNextPoint(vec.toArray());
            vtkVertex vert=new vtkVertex();
            vert.GetPointIds().SetId(0, id);
            cells.InsertNextCell(vert);
            radiusArray.InsertNextValue(vec.getNorm());
        }
        vtkPolyData polyData=new vtkPolyData();
        polyData.SetPoints(pts);
        polyData.SetVerts(cells);
        polyData.GetCellData().AddArray(radiusArray);

        String vtkFileName="/Users/zimmemi1/Desktop/test.vtk";
        vtkPolyDataWriter writer2=new vtkPolyDataWriter();
        writer2.SetFileName(vtkFileName);
        writer2.SetFileTypeToBinary();
        writer2.SetInputData(polyData);
        writer2.Write();
        System.out.println("Wrote to "+vtkFileName);

    }

    private static void makeTree(String[] args) throws IOException, HyperException
    {
        //String inputDirectoryString=args[0];    // "/Volumes/dumbledore/sbmt/OLA"
        String inputDirectoryListFileString=args[0];
        String outputDirectoryString=args[1];   // "/Volumes/dumbledore/sbmt/ola_hypertree"
        double dataFileMBLimit=Double.valueOf(args[2]); // 1
        int maxNumOpenOutputFiles=Integer.valueOf(args[3]);   // 32

        int nFilesToProcess=Integer.valueOf(args[4]);

        System.out.println("Input data directory listing = "+inputDirectoryListFileString);
        System.out.println("Output tree location = "+outputDirectoryString);
        System.out.println("Data file MB limit = "+dataFileMBLimit);
        System.out.println("Max # open output files = "+maxNumOpenOutputFiles);

        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Path inputDirectoryListFile=Paths.get(inputDirectoryListFileString);
        Path outputDirectory=Paths.get(outputDirectoryString);

        int dataFileByteLimit=(int)(dataFileMBLimit*1024*1024);
        int maxPointsPerLeaf=dataFileByteLimit/new OlaFSHyperPoint().getSizeInBytes();   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        DataOutputStreamPool pool=new DataOutputStreamPool(maxNumOpenOutputFiles);

        Configuration.setAPLVersion(true);
        ShapeModelBody body=ShapeModelBody.RQ36;
        ShapeModelAuthor author=ShapeModelAuthor.GASKELL;
        String version="V3 Image";
        Bennu bennu = new Bennu(SmallBodyViewConfig.getSmallBodyConfig(body,author,version));
        BoundingBox bbox=new BoundingBox(bennu.getBoundingBox().getBounds());
        System.out.println("Shape model info:");
        System.out.println("  Body = "+body);
        System.out.println("  Author = "+author);
        System.out.println("  Version = \""+version+"\"");
        System.out.println("Original bounding box = "+bbox);
        double bboxSizeIncrease=0.05;
        bbox.increaseSize(bboxSizeIncrease);
        System.out.println("Bounding box diagonal length increase = "+bboxSizeIncrease);
        System.out.println("Rescaled bounding box = "+bbox);
        System.out.println();

        // time limits from OlaBruteForceTimeRangeCalculator
        double tmin=6.0e8;
        double tmax=6.1e8;
        double tscale=(tmax-tmin)*bboxSizeIncrease/2.;
        double newTmin=tmin-tscale;
        double newTmax=tmax+tscale;
        System.out.println("tmin="+tmin+" tmax="+tmax+" are being expanded by a factor of "+bboxSizeIncrease+" to tmin="+newTmin+" tmax="+newTmax);
        HyperBox hbox=new HyperBox(new double[]{bbox.xmin, bbox.ymin, bbox.zmin, tmin}, new double[]{bbox.xmax, bbox.ymax, bbox.zmax, tmax});

        List<File> fileList=Lists.newArrayList();
        Scanner scanner=new Scanner(inputDirectoryListFile.toFile());
        while (scanner.hasNextLine())
        {
            File dataDirectory=inputDirectoryListFile.getParent().resolve(scanner.nextLine().trim()).toFile();
            System.out.println("Searching for .l2 files in "+dataDirectory.toString());
            Collection<File> fileCollection=FileUtils.listFiles(dataDirectory, new WildcardFileFilter("*.l2"), null);
            for (File f : fileCollection) {
                System.out.println("Adding file "+f+" to the processing queue");
                fileList.add(f);
            }
        }
        scanner.close();
        int numFiles=fileList.size();

        if (nFilesToProcess>-1)
            numFiles=nFilesToProcess;

        //FileUtils.deleteDirectory(outputDirectory.toFile());
        FileUtils.forceMkdir(outputDirectory.toFile());
        OlaFSHyperTreeGenerator generator=new OlaFSHyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles,pool);

        Stopwatch sw=new Stopwatch();
        for (int i=0; i<numFiles; i++) {
            sw.reset();
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println("Searching for valid lidar points in file "+(i+1)+"/"+numFiles+" : "+inputPath);
            generator.addPointsFromL2FileToRoot(inputPath,Integer.MAX_VALUE);
            System.out.println("  Elapsed time = "+sw.elapsedTime(TimeUnit.SECONDS)+" s");
            System.out.println("  Total points written into master data file = "+generator.totalPointsWritten);// TODO: close down all DataOutputStreams
            System.out.println("  Total MB written into master data file = "+generator.convertBytesToMB(generator.root.getDataFilePath().toFile().length()));
        }
        long rootFileSizeBytes=generator.root.getDataFilePath().toFile().length();

        sw.reset();
        sw.start();
        System.out.println("Expanding tree.");
        System.out.println("Max # pts per leaf="+maxPointsPerLeaf);
        System.out.println("   ... equivalent to "+dataFileMBLimit+" MB max per file");
        generator.expand();
        System.out.println("Done expanding tree. Time elapsed="+sw.elapsedTime(TimeUnit.SECONDS)+" s");
        System.out.println("Cleaning up.");
        System.out.println();
        generator.commit(); // clean up any empty or open data files

        System.out.println("Total MB stored = "+generator.convertBytesToMB(generator.countBytes()));
        System.out.println("Total MB initially copied = "+generator.convertBytesToMB(rootFileSizeBytes));

        Path fileMapPath=outputDirectory.resolve("fileMap.txt");
        System.out.print("Writing file map to "+fileMapPath+"... ");
        FileWriter writer=new FileWriter(fileMapPath.toFile());
        for (int i : generator.fileMap.inverse().keySet())
            writer.write(i+" "+generator.fileMap.inverse().get(i)+"\n");
        writer.close();
        System.out.println("Done.");
    }
}
