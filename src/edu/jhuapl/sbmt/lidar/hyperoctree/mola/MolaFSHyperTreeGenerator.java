package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.File;
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

import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperPoint;
import edu.jhuapl.sbmt.lidar.test.DataOutputStreamPool;

public class MolaFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public MolaFSHyperTreeGenerator(Path outputDirectory,
            int maxNumberOfPointsPerLeaf, HyperBox bbox,
            int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        super(outputDirectory, maxNumberOfPointsPerLeaf, bbox,
                maxNumberOfOpenOutputFiles, pool);
        // XXX Auto-generated constructor stub
    }

    public void addPointsFromFileToRoot(Path inputFile) throws HyperException, IOException
    {
        int fileNum=-1;
        if (!fileMap.containsKey(inputFile))
        {
            fileNum=fileMap.size();
            fileMap.put(inputFile, fileNum);
        }
        else
            fileNum=fileMap.get(inputFile);


        Scanner scanner=new Scanner(inputFile.toFile());
        scanner.nextLine();
        while (scanner.hasNextLine())
        {
            String line=scanner.nextLine();
            String[] tokens=line.trim().split("\\s+");   // split on whitespace, cf. http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
            if (tokens.length==0)
                break;
            double lon=Double.valueOf(tokens[0]);   // degrees
            double lat=Double.valueOf(tokens[1]);   // degrees
            double r=Double.valueOf(tokens[2])/1000;            // convert to km
            double time=Double.valueOf(tokens[5]);
            double intensity=0;
            //
            double[] xyz = MathUtil.latrec(new LatLon(lat/180.*Math.PI, lon/180.*Math.PI, r));
            Vector3D tgpos=new Vector3D(xyz[1],xyz[0],xyz[2]);
            Vector3D scpos=Vector3D.ZERO;
            //
            root.add(new MolaFSHyperPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), intensity, fileNum));
        }
    }

    public static void main(String[] args) throws IOException, HyperException
    {
        String inputFileName=args[0];
        String outputDirectoryString=args[1];   // "/Volumes/dumbledore/sbmt/ola_hypertree"
        double dataFileMBLimit=Double.valueOf(args[2]); // 1
        int maxNumOpenOutputFiles=Integer.valueOf(args[3]);   // 32
        int nFilesToProcess=Integer.valueOf(args[4]);

        System.out.println("Input data file = "+inputFileName);
        System.out.println("Output tree location = "+outputDirectoryString);
        System.out.println("Data file MB limit = "+dataFileMBLimit);
        System.out.println("Max # open output files = "+maxNumOpenOutputFiles);

        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Path outputDirectory=Paths.get(outputDirectoryString);

        int dataFileByteLimit=(int)(dataFileMBLimit*1024*1024);
        int maxPointsPerLeaf=dataFileByteLimit/new OlaFSHyperPoint().getSizeInBytes();   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        DataOutputStreamPool pool=new DataOutputStreamPool(maxNumOpenOutputFiles);

        MolaFSLidarPointBoundsCalculator calc=new MolaFSLidarPointBoundsCalculator(inputFileName);

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
        File inputFile=new File(inputFileName);
        Scanner scanner=new Scanner(inputFile);
        while (scanner.hasNextLine())
        {
            File dataDirectory=Paths.get(inputFile.getParent()).resolve(scanner.nextLine().trim()).toFile();
            System.out.println("Searching for "+MolaFSLidarPointBoundsCalculator.molaFileExtension+" files in "+dataDirectory.toString());
            Collection<File> fileCollection=FileUtils.listFiles(dataDirectory, new WildcardFileFilter("*."+MolaFSLidarPointBoundsCalculator.molaFileExtension), null);
            for (File f : fileCollection) {
                System.out.println("Adding file "+f+" to the processing queue");
                fileList.add(f);
            }
        }
        scanner.close();
        int numFiles=fileList.size();

        if (nFilesToProcess>-1)
            numFiles=nFilesToProcess;

        FileUtils.forceMkdir(outputDirectory.toFile());
        MolaFSHyperTreeGenerator generator=new MolaFSHyperTreeGenerator(outputDirectory, maxPointsPerLeaf, hbox, maxNumOpenOutputFiles,pool);

        Stopwatch sw=new Stopwatch();
        for (int i=0; i<numFiles; i++) {
            sw.reset();
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println("Searching for valid lidar points in file "+(i+1)+"/"+numFiles+" : "+inputPath);
            generator.addPointsFromFileToRoot(inputPath);
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
