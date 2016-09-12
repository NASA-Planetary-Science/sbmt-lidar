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
import edu.jhuapl.sbmt.lidar.hyperoctree.Dimensioned;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;

public class OlaFSHyperTreeStatistics implements Dimensioned
{
    int numBinsPerDimension;
    double[][] binCounts;   // [# dim][# bins]
    double[][] binEdges;    // [# dim][# bins + 1]
    int pointsConsidered=0;

    public OlaFSHyperTreeStatistics(int numBinsPerDimension, double[] bounds)
    {
        this.numBinsPerDimension=numBinsPerDimension;
        binCounts=new double[getDimension()][numBinsPerDimension];
        binEdges=new double[getDimension()][];
        for (int d=0; d<getDimension(); d++)
            binEdges[d]=linspace(bounds[2*d+0], bounds[2*d+1], numBinsPerDimension+1);
    }

    public void save(Path outFile)
    {
        FileWriter writer;
        try
        {
            writer = new FileWriter(outFile.toFile());
            writer.write(getDimension()+" "+numBinsPerDimension+"\n");
            for (int i=0; i<getDimension(); i++)
            {
                for (int b=0; b<=numBinsPerDimension; b++)
                    writer.write(binEdges[i][b]+" ");
                writer.write("\n");
            }
            for (int i=0; i<getDimension(); i++)
            {
                for (int b=0; b<numBinsPerDimension; b++)
                    writer.write(binCounts[i][b]+" ");
                writer.write("\n");
            }
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public int getDimension()
    {
        return 4;
    }

    private double[] linspace(double a, double b, int n)
    {
        double[] arr=new double[n];
        for (int i=0; i<n; i++)
            arr[i]=(double)i/(double)(n-1)*(b-a)+a;
        return arr;
    }

    private int getBin(int dim, double val)
    {
  //      System.out.println(dim+" "+val+" "+binEdges[dim][0]+" "+binEdges[dim][numBinsPerDimension]);
         return (int)((val-binEdges[dim][0])/(binEdges[dim][numBinsPerDimension]-binEdges[dim][0])*(numBinsPerDimension-1));
    }

    public void addPointsFromL2FileToRoot(Path l2FilePath, int maxPoints)
    {
        int cnt=0;
        try
        {
            String filePathString=l2FilePath.toString();
            if (!filePathString.endsWith(".l2"))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected .l2");

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
//                    root.add(new OlaFSHyperPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), intensity));
//                    totalPointsWritten++;
                    OlaFSHyperPoint pt=new OlaFSHyperPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), intensity, -1);    // -1 means don't worry about file number
                    for (int i=0; i<getDimension(); i++)
                        binCounts[i][getBin(i, pt.getCoordinate(i))]++;
                    cnt++;
                    pointsConsidered++;
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

    public double convertBytesToMB(long bytes)
    {
        return (double)bytes/(double)(1024*1024);
    }

    public static void main(String[] args) throws HyperException, IOException
    {
        //String inputDirectoryString=args[0];    // "/Volumes/dumbledore/sbmt/OLA"
        String inputDirectoryListFileString=args[0];
        String outputDirectoryString=args[1];   // "/Volumes/dumbledore/sbmt/ola_hypertree"
        //double dataFileMBLimit=Double.valueOf(args[2]); // 1
        //int maxNumOpenOutputFiles=Integer.valueOf(args[3]);   // 32

        System.out.println("Input data directory listing = "+inputDirectoryListFileString);
        System.out.println("Output tree location = "+outputDirectoryString);

        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Path inputDirectoryListFile=Paths.get(inputDirectoryListFileString);
        Path outputDirectory=Paths.get(outputDirectoryString);

/*        Configuration.setAPLVersion(true);
        ShapeModelBody body=ShapeModelBody.RQ36;
        ShapeModelAuthor author=ShapeModelAuthor.GASKELL;
        String version="V3 Image";
        Bennu bennu = new Bennu(SmallBodyConfig.getSmallBodyConfig(body,author,version));
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
/*        double tmin=6.0e8;
        double tmax=6.1e8;
        double tscale=(tmax-tmin)*bboxSizeIncrease/2.;
        double newTmin=tmin-tscale;
        double newTmax=tmax+tscale;
        System.out.println("tmin="+tmin+" tmax="+tmax+" are being expanded by a factor of "+bboxSizeIncrease+" to tmin="+newTmin+" tmax="+newTmax);
*/
        double tmin=6.006788691942868E8;
        double tmax=6.095590075293242E8;
        double xmin=-1.0332293001977328;
        double xmax=1.0615876654593568;
        double ymin=-1.0361160808458427;
        double ymax=1.0481285332598738;
        double zmin=-0.31806753854052083;
        double zmax=0.35328164609553353;
        System.out.println("original bounds: t=["+tmin+" "+tmax+"] x=["+xmin+" "+xmax+"] y=["+ymin+" "+ymax+"] z=["+zmin+" "+zmax+"]");

        double Lt=tmax-tmin;
        double Lx=xmax-xmin;
        double Ly=ymax-ymin;
        double Lz=zmax-zmin;
        double tmid=(tmax+tmin)/2;
        double xmid=(xmax+xmin)/2;
        double ymid=(ymax+ymin)/2;
        double zmid=(zmax+zmin)/2;
        double scfac=1.01;
        tmin=tmid-Lt/2*scfac;
        tmax=tmid+Lt/2*scfac;
        xmin=xmid-Lx/2*scfac;
        xmax=xmid+Lx/2*scfac;
        ymin=ymid-Ly/2*scfac;
        ymax=ymid+Ly/2*scfac;
        zmin=zmid-Lz/2*scfac;
        zmax=zmid+Lz/2*scfac;
        System.out.println("expanded bounds (scfac="+scfac+"): t=["+tmin+" "+tmax+"] x=["+xmin+" "+xmax+"] y=["+ymin+" "+ymax+"] z=["+zmin+" "+zmax+"]");

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
        //if (nFilesToProcess>-1)
        //    numFiles=nFilesToProcess;
        //numFiles=20;

        //FileUtils.deleteDirectory(outputDirectory.toFile());
        FileUtils.forceMkdir(outputDirectory.toFile());
        OlaFSHyperTreeStatistics statistics=new OlaFSHyperTreeStatistics(64,hbox.getBounds());

        Stopwatch sw=new Stopwatch();
        for (int i=0; i<numFiles; i++) {
            sw.reset();
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println("Searching for valid lidar points in file "+(i+1)+"/"+numFiles+" : "+inputPath);
            statistics.addPointsFromL2FileToRoot(inputPath,Integer.MAX_VALUE);
            System.out.println("  Elapsed time = "+sw.elapsedTime(TimeUnit.SECONDS)+" s");
            System.out.println("  Total points considered = "+statistics.pointsConsidered);// TODO: close down all DataOutputStreams
        }
        Path outputFile=outputDirectory.resolve("stats");
        System.out.print("Writing to "+outputFile+"... ");
        statistics.save(outputFile);
        System.out.println("Done.");

    }
}
