package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
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
import edu.jhuapl.sbmt.core.io.DataOutputStreamPool;
import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2.Hayabusa2HyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.nlr.NlrFSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.misc.LidarInstrument;
import edu.jhuapl.sbmt.lidar.misc.RawLidarFile;
import edu.jhuapl.sbmt.query.hyperoctree.FSHyperTreeNode;
import edu.jhuapl.sbmt.query.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.query.hyperoctree.HyperException;

public abstract class FSHyperTreeGenerator
{
    final Path outputDirectory;
    final int maxNumberOfPointsPerLeaf;
    final HyperBox bbox;
    final int maxNumberOfOpenOutputFiles;
    final DataOutputStreamPool pool;
    private FSHyperTreeNode root;
    private long totalPointsWritten=0;
    private long totalPoints = 0;

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
//        LidarDataPerUnit lidarData = new LidarDataPerUnit(inputPath, bodyConfig, );
        getFileMap().put(inputPath.getFileName(),file.getFileNumber());
        Iterator<LidarPoint> iterator=file.iterator();
        while (iterator.hasNext())
        {
            if( getRoot().add(FSHyperPointWithFileTag.wrap(iterator.next(),file.getFileNumber())) )
                setTotalPointsWritten(getTotalPointsWritten() + 1);
            totalPoints++; // count all points, whether written or not for debug purposes

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
        	System.out.println("FSHyperTreeGenerator: expandNode: splitting, max num  "  + maxNumberOfPointsPerLeaf + " and this node has " + node.getNumberOfPoints());
            node.split();
            for (int i=0; i<node.getNumberOfChildren(); i++)
                if (node.childExists(i))
                {
                    System.out.println("Expanding node: " + node.getChild(i).getPath());
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
            for (int i=0; i<node.getNumberOfChildren(); i++)
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
        System.out.println("  (3) instrument name (options are "
                + Arrays.toString(LidarInstrument.values())
                + ")");
        System.out.println("  (4) start date for tree (yyyy-MM-ddTHH:mm:ss.000)");
        System.out.println("  (5) end date for tree (yyyy-MM-ddTHH:mm:ss.000)");


    }


    public static void main(String[] args) throws IOException, HyperException, ParseException
    {
    	if (args.length < 5 || args.length > 6)
        {
            printUsage();
            return;
        }

        String inputDirectoryListFileString = args[0];
        String outputDirectoryString = args[1];
        String instrumentName = args[2];
        double startDate = TimeUtil.str2et(args[3]);
        double stopDate  = TimeUtil.str2et(args[4]);

        double dataFileMBLimit = 1; //.05;
        if (args.length == 6)
        	dataFileMBLimit = Double.parseDouble(args[5]);
        int maxNumOpenOutputFiles = 32;


        System.out.println("Input data directory listing = "+inputDirectoryListFileString);
        System.out.println("Output tree location = "+outputDirectoryString);
        System.out.println("Data file MB limit = "+dataFileMBLimit);
        System.out.println("Max # open output files = "+maxNumOpenOutputFiles);
        System.out.println("Instrument = "+instrumentName);

        // set up environment
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();
        Path inputDirectoryListFile=Paths.get(inputDirectoryListFileString);
        Path outputDirectory=Paths.get(outputDirectoryString);

        int dataFileByteLimit = (int)(dataFileMBLimit*1024*1024);
        int maxPointsPerLeaf = dataFileByteLimit/new FSHyperPointWithFileTag().getSizeInBytes();
        DataOutputStreamPool pool = new DataOutputStreamPool(maxNumOpenOutputFiles);

        LidarInstrument instrument = LidarInstrument.valueOf(instrumentName);
        BoundingBox bbox = instrument.getBoundingBox();
        System.out.println("Original bounding box = "+bbox);
        double bboxSizeIncrease=0.05;
        bbox.increaseSize(bboxSizeIncrease);
        System.out.println("Bounding box diagonal length increase = "+bboxSizeIncrease);
        System.out.println("Rescaled bounding box = "+bbox);
        System.out.println();

        double tmin = startDate;
        double tmax = stopDate;
        System.out.println("tmin = " + tmin + " tmax= " + tmax + " are being expanded by a factor of " + bboxSizeIncrease + " to tmin = " + tmin + " tmax = " + tmax);
        HyperBox hbox=new HyperBox(new double[]{bbox.xmin, bbox.ymin, bbox.zmin, tmin}, new double[]{bbox.xmax, bbox.ymax, bbox.zmax, tmax});

        List<File> fileList=Lists.newArrayList();
        Scanner scanner=new Scanner(inputDirectoryListFile.toFile());
        while (scanner.hasNextLine())
        {
            File dataDirectory = inputDirectoryListFile.getParent().resolve(scanner.nextLine().trim()).toFile();
            System.out.println(dataDirectory + " " + dataDirectory.isDirectory() + " " + dataDirectory.getName());
            System.out.println("Searching for ." + instrument.getRawFileExtension() + " files in " + dataDirectory.toString());
            Collection<File> fileCollection = FileUtils.listFiles(dataDirectory, new WildcardFileFilter("*."+instrument.getRawFileExtension()), null);
            for (File f : fileCollection) {
                System.out.println("Adding file "+f+" to the processing queue");
                fileList.add(f);
            }
        }
        scanner.close();
        int numFiles = fileList.size();

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
            hbox=new HyperBox(new double[]{bbox.xmin * 1e-3, bbox.ymin* 1e-3, bbox.zmin* 1e-3, tmin, 0}, new double[]{bbox.xmax* 1e-3, bbox.ymax* 1e-3, bbox.zmax* 1e-3, tmax, 1e7});
            generator=new Hayabusa2HyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles, pool);
            break;
        }

        Stopwatch sw=Stopwatch.createUnstarted();
        for (int i=0; i<numFiles; i++) {
            sw.reset();
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());

            /*
             * Check if file is in time range
             */
            if (instrument == LidarInstrument.OLA) {
                try {
                    String filename = inputPath.getFileName().toString();
                    String[] toks = filename.split("_");
                    String date = toks[0];
                    String strDate = String.format("20%s-%s-%sT00:00:00.000", date.substring(2,4), date.substring(4,6), date.substring(6,8));
                    double et = TimeUtil.str2et(strDate);
                    if (et >= tmin && et <= tmax) {
                        System.out.println("Searching for valid lidar points in file "+(i+1)+"/"+numFiles+" : "+inputPath);
                        generator.addAllPointsFromFile(inputPath);
                    }

                } catch (Exception e) {
                    System.out.println("Filename not formatted as expected (i.e. 181216_ola_scil2id01007.dat) so unable to parse date from filename.");
                }
            } else {
                // for other instruments, just add all points without checking filename for now
                generator.addAllPointsFromFile(inputPath);
            }
        }

        System.out.println("  Elapsed time = "+sw.elapsed(TimeUnit.SECONDS)+" s");
        System.out.println("  Total points from all files = "+generator.getTotalPoints());
        System.out.println("  Total points written into master data file = "+generator.getTotalPointsWritten());
        System.out.println("  Total MB written into master data file = "+generator.convertBytesToMB(generator.getRoot().getDataFilePath().toFile().length()));

        long rootFileSizeBytes = generator.getRoot().getDataFilePath().toFile().length();

        sw.reset();
        sw.start();
        System.out.println("Expanding tree.");
        System.out.println("Max # pts per leaf="+maxPointsPerLeaf);
        System.out.println("   ... equivalent to "+dataFileMBLimit+" MB max per file");
        generator.expand();
        System.out.println("Done expanding tree. Time elapsed="+sw.elapsed(TimeUnit.SECONDS)+" s");
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

    private long getTotalPoints()
    {
        // TODO Auto-generated method stub
        return totalPoints;
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
