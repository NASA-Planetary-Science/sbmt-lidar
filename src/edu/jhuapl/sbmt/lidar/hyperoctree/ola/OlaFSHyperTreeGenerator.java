package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

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
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;
import edu.jhuapl.sbmt.lidar.test.DataOutputStreamPool;

public class OlaFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public OlaFSHyperTreeGenerator(Path outputDirectory,
            int maxNumberOfPointsPerLeaf, HyperBox bbox,
            int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        super(outputDirectory, maxNumberOfPointsPerLeaf, bbox,
                maxNumberOfOpenOutputFiles, pool);
        // TODO Auto-generated constructor stub
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


    public static void main(String[] args) throws IOException, HyperException
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

        OlaFSLidarPointBoundsCalculator calc=new OlaFSLidarPointBoundsCalculator(inputDirectoryListFileString);

        double bboxSizeIncrease=0.01;
        double tmin=calc.tmin;
        double tmax=calc.tmax;
        double xmin=calc.xmin;
        double ymin=calc.ymin;
        double zmin=calc.zmin;
        double xmax=calc.xmax;
        double ymax=calc.ymax;
        double zmax=calc.zmax;
        System.out.println("Original bounds: (t x y z)=("+tmin+" "+xmin+" "+ymin+" "+zmin+")-("+tmax+" "+xmax+" "+ymax+" "+zmax+")");

        double tscale=(tmax-tmin)*bboxSizeIncrease/2.;
        tmin-=tscale;
        tmax+=tscale;
        double xscale=(xmax-xmin)*bboxSizeIncrease/2.;
        xmin-=xscale;
        xmax+=xscale;
        double yscale=(ymax-ymin)*bboxSizeIncrease/2.;
        ymin-=yscale;
        ymax+=yscale;
        double zscale=(zmax-zmin)*bboxSizeIncrease/2.;
        zmin-=zscale;
        zmax+=zscale;
        System.out.println("Using expanded bounds instead: (t x y z)=("+tmin+" "+xmin+" "+ymin+" "+zmin+")-("+tmax+" "+xmax+" "+ymax+" "+zmax+")");
        HyperBox hbox=new HyperBox(new double[]{xmin, ymin, zmin, tmin}, new double[]{xmax, ymax, zmax, tmax});

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
        System.out.println("Done generating tree.");
    }

}
