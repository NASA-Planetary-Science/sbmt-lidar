package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.LidarInstrument;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.RawLidarFile;
import edu.jhuapl.sbmt.lidar.hyperoctree.laser.Hayabusa2LaserFSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.nlr.NlrFSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperPoint;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeNode;

public abstract class FSHyperTreeGenerator
{
    final Path outputDirectory;
    final int maxNumberOfPointsPerLeaf;
    final HyperBox bbox;
    final int maxNumberOfOpenOutputFiles;
    final DataOutputStreamPool pool;
    private FSHyperTreeNode root;
    private long totalPointsWritten=0;

    private BiMap<Path, Integer> fileMap=HashBiMap.create();

    public FSHyperTreeGenerator(Path outputDirectory, int maxNumberOfPointsPerLeaf, HyperBox bbox, int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        this.outputDirectory=outputDirectory;
        this.maxNumberOfPointsPerLeaf=maxNumberOfPointsPerLeaf;
        this.maxNumberOfOpenOutputFiles=maxNumberOfOpenOutputFiles;
        this.bbox=bbox;
        this.pool=pool;
        setRoot(new OlaFSHyperTreeNode(null, outputDirectory, bbox, maxNumberOfPointsPerLeaf,pool));
    }

    public void addAllPointsFromFile(Path inputPath) throws HyperException, IOException
    {
        RawLidarFile file=openFile(inputPath);
        getFileMap().put(inputPath.getFileName(),file.getFileNumber());
        Iterator<LidarPoint> iterator=file.iterator();
        while (iterator.hasNext())
        {
            getRoot().add(FSHyperPointWithFileTag.wrap(iterator.next(),file.getFileNumber()));
            setTotalPointsWritten(getTotalPointsWritten() + 1);
        }
    }

    public abstract RawLidarFile openFile(Path file);

    public void expand() throws HyperException, IOException
    {
        expandNode(getRoot());
    }

    public void expandNode(FSHyperTreeNode node) throws HyperException, IOException
    {
        if (node.getNumberOfPoints()>maxNumberOfPointsPerLeaf)
        {
            node.split();
            for (int i=0; i<node.getNumberOfChildren(); i++)
                if (node.childExists(i))
                {
                    System.out.println(node.getChild(i).getPath());
                    expandNode((FSHyperTreeNode)node.getChild(i));
                }
        }
    }

    public void commit() throws IOException
    {
        pool.closeAllStreams();// close any files that are still open
        finalCommit(getRoot());
    }

    void finalCommit(FSHyperTreeNode node) throws IOException
    {
        File dataFile=node.getDataFilePath().toFile();  // clean up any data files with zero points
        if (!node.isLeaf())
        {
            if (dataFile.exists())
                dataFile.delete();
            for (int i=0; i<8; i++)
                finalCommit((FSHyperTreeNode)node.getChild(i));
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
        List<FSHyperTreeNode> nodeList=getAllNonEmptyLeafNodes();
        long total=0;
        for (FSHyperTreeNode node : nodeList)
            total+=node.getDataFilePath().toFile().length();
        return total;
    }

    public List<FSHyperTreeNode> getAllNonEmptyLeafNodes()
    {
        List<FSHyperTreeNode> nodeList=Lists.newArrayList();
        getAllNonEmptyLeafNodes(getRoot(), nodeList);
        return nodeList;
    }

    void getAllNonEmptyLeafNodes(FSHyperTreeNode node, List<FSHyperTreeNode> nodeList)
    {
        if (!node.isLeaf())
            for (int i=0; i<node.getNumberOfChildren(); i++)
                getAllNonEmptyLeafNodes(node.getChild(i), nodeList);
        else if (node.getDataFilePath().toFile().exists())
            nodeList.add(node);
    }

    private static void printUsage()
    {
        System.out.println("Arguments:");
        System.out.println("  (1) file containing list of input directories, each separated by newline");
        System.out.println("  (2) output directory to build the search tree in");
        System.out.println("  (3) data file MB limit");
        System.out.println("  (4) max number of open output files");
        System.out.println("  (5) total number of files in the list to process (-1 for all)");
        System.out.println("  (6) instrument name (options are "
                + Arrays.toString(LidarInstrument.values())
                + ")");
    }


    public static void main(String[] args) throws IOException, HyperException
    {
        if (args.length!=6)
        {
            printUsage();
            return;
        }

        //String inputDirectoryString=args[0];    // "/Volumes/dumbledore/sbmt/OLA"
        String inputDirectoryListFileString=args[0];
        String outputDirectoryString=args[1];   // "/Volumes/dumbledore/sbmt/ola_hypertree"
        double dataFileMBLimit=Double.valueOf(args[2]); // 1
        int maxNumOpenOutputFiles=Integer.valueOf(args[3]);   // 32
        int nFilesToProcess=Integer.valueOf(args[4]);
        String instrumentName=args[5];

        System.out.println("Input data directory listing = "+inputDirectoryListFileString);
        System.out.println("Output tree location = "+outputDirectoryString);
        System.out.println("Data file MB limit = "+dataFileMBLimit);
        System.out.println("Max # open output files = "+maxNumOpenOutputFiles);
        System.out.println("Number of files to process = "+nFilesToProcess);
        System.out.println("Instrument = "+instrumentName);

        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Path inputDirectoryListFile=Paths.get(inputDirectoryListFileString);
        Path outputDirectory=Paths.get(outputDirectoryString);

        int dataFileByteLimit=(int)(dataFileMBLimit*1024*1024);
        int maxPointsPerLeaf=dataFileByteLimit/new OlaFSHyperPoint().getSizeInBytes();   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        DataOutputStreamPool pool=new DataOutputStreamPool(maxNumOpenOutputFiles);

        LidarInstrument instrument=LidarInstrument.valueOf(instrumentName);
        BoundingBox bbox=instrument.getBoundingBox();
        System.out.println("Original bounding box = "+bbox);
        double bboxSizeIncrease=0.05;
        bbox.increaseSize(bboxSizeIncrease);
        System.out.println("Bounding box diagonal length increase = "+bboxSizeIncrease);
        System.out.println("Rescaled bounding box = "+bbox);
        System.out.println();

        double tmin=instrument.getTmin();
        double tmax=instrument.getTmax();
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
            System.out.println(dataDirectory+" "+dataDirectory.isDirectory()+" "+dataDirectory.getName());
            System.out.println("Searching for ."+instrument.getRawFileExtension()+" files in "+dataDirectory.toString());
            Collection<File> fileCollection=FileUtils.listFiles(dataDirectory, new WildcardFileFilter("*."+instrument.getRawFileExtension()), null);
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
        //FileUtils.forceMkdir(outputDirectory.toFile());
        if (!outputDirectory.toFile().exists())
        {
            System.out.println("Error: Output directory \""+outputDirectory.toString()+"\" does not exist");
            return;
        }
        else
        {
            System.out.println();
            System.out.println("++++++++++++++++++");
            System.out.println("Warning: output directory \""+outputDirectoryString.toString()+"\" already exists; if it is not empty this will cause big problems later. ");
            System.out.println("++++++++++++++++++");
            System.out.println();
        }


        FSHyperTreeGenerator generator=null;
        switch (instrument)
        {
        case OLA:
            generator=new OlaFSHyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles, pool);
            break;
        case NLR:
            generator=new NlrFSHyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles, pool);
            break;
        case LASER:
            // add min/max range to the root hyper box  TODO what should original max range be?
            hbox=new HyperBox(new double[]{bbox.xmin, bbox.ymin, bbox.zmin, tmin, 0}, new double[]{bbox.xmax, bbox.ymax, bbox.zmax, tmax, 1e10});
            generator=new Hayabusa2LaserFSHyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles, pool);
            break;
        }

        Stopwatch sw=new Stopwatch();
        for (int i=0; i<numFiles; i++) {
            sw.reset();
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println("Searching for valid lidar points in file "+(i+1)+"/"+numFiles+" : "+inputPath);


            if (generator instanceof Hayabusa2LaserFSHyperTreeGenerator) {
                ((Hayabusa2LaserFSHyperTreeGenerator)generator).addAllPointsFromFile(inputPath);
            }
            else {
                generator.addAllPointsFromFile(inputPath);
            }


            System.out.println("  Elapsed time = "+sw.elapsedTime(TimeUnit.SECONDS)+" s");
            System.out.println("  Total points written into master data file = "+generator.getTotalPointsWritten());// TODO: close down all DataOutputStreams
            System.out.println("  Total MB written into master data file = "+generator.convertBytesToMB(generator.getRoot().getDataFilePath().toFile().length()));
        }
        long rootFileSizeBytes=generator.getRoot().getDataFilePath().toFile().length();

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
        for (int i : generator.getFileMap().inverse().keySet())
            writer.write(i+" "+generator.getFileMap().inverse().get(i)+"\n");
        writer.close();
        System.out.println("Done.");
    }

    public BiMap<Path, Integer> getFileMap()
    {
        return fileMap;
    }

    public void setFileMap(BiMap<Path, Integer> fileMap)
    {
        this.fileMap = fileMap;
    }

    public FSHyperTreeNode getRoot()
    {
        return root;
    }

    public void setRoot(FSHyperTreeNode root)
    {
        this.root = root;
    }

    public long getTotalPointsWritten()
    {
        return totalPointsWritten;
    }

    public void setTotalPointsWritten(long totalPointsWritten)
    {
        this.totalPointsWritten = totalPointsWritten;
    }
}
