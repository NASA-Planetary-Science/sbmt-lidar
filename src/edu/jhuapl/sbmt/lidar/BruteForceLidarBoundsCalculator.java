package edu.jhuapl.sbmt.lidar;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.hyperoctree.nlr.NlrRawLidarFile;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaLidarFile;


public class BruteForceLidarBoundsCalculator
{
    double tmin=Double.POSITIVE_INFINITY;
    double tmax=Double.NEGATIVE_INFINITY;
    double xmin=Double.POSITIVE_INFINITY;
    double xmax=Double.NEGATIVE_INFINITY;
    double ymin=Double.POSITIVE_INFINITY;
    double ymax=Double.NEGATIVE_INFINITY;
    double zmin=Double.POSITIVE_INFINITY;
    double zmax=Double.NEGATIVE_INFINITY;

    LidarInstrument instrument;

    public BruteForceLidarBoundsCalculator(LidarInstrument instrument)
    {
        this.instrument=instrument;
    }

    public RawLidarFile openFile(Path file)
    {
        switch (instrument)
        {
        case OLA:
            return new OlaLidarFile(file.toString());
        case NLR:
            return new NlrRawLidarFile(file.toString());
        default:
            return null;
        }
    }

    public static void printUsage()
    {
        System.out.println("Arguments:");
        System.out.println("  (1) file containing list of input directories, each separated by newline");
        System.out.println("  (2) lidar instrument (one of "+Arrays.toString(LidarInstrument.values())+")");
    }

    public static void main(String[] args) throws FileNotFoundException
    {
        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Configuration.setAPLVersion(true);

        String inputDirectoryListFileString=args[0];
        LidarInstrument instrument=LidarInstrument.valueOf(LidarInstrument.class, args[1]);

        System.out.println("Input data directory list found in file "+inputDirectoryListFileString);
        System.out.println("Instrument "+instrument.name()+" +found");

        Path inputDirectoryListFile=Paths.get(inputDirectoryListFileString);
        List<Path> fileList=Lists.newArrayList();
        Scanner scanner=new Scanner(inputDirectoryListFile.toFile());
        while (scanner.hasNextLine())
        {
            File dataDirectory=inputDirectoryListFile.getParent().resolve(scanner.nextLine().trim()).toFile();
            System.out.println("Searching for "+instrument.getRawFileExtension()+" files in "+dataDirectory.toString());
            Collection<File> fileCollection=FileUtils.listFiles(dataDirectory, new WildcardFileFilter("*."+instrument.getRawFileExtension()), null);
            for (File f : fileCollection) {
                System.out.println("Adding file "+f+" to the processing queue");
                fileList.add(f.toPath());
            }
        }
        scanner.close();

        BruteForceLidarBoundsCalculator calc=new BruteForceLidarBoundsCalculator(instrument);
        System.out.println("Processing files...");
        for (int i=0; i<fileList.size(); i++)
        {
            calc.getBounds(fileList.get(i));
            System.out.println("File "+i+"/"+fileList.size()+":  tmin="+calc.tmin+" tmax="+calc.tmax+"  xmin="+calc.xmin+" xmax="+calc.xmax+"  ymin="+calc.ymin+" ymax="+calc.ymax+"  zmin="+calc.zmin+" zmax="+calc.zmax);
        }

        System.out.println("Final results:  tmin="+calc.tmin+" tmax="+calc.tmax+"  xmin="+calc.xmin+" xmax="+calc.xmax+"  ymin="+calc.ymin+" ymax="+calc.ymax+"  zmin="+calc.zmin+" zmax="+calc.zmax);

    }

    public void getBounds(Path f)
    {
        Iterator<LidarPoint> it=openFile(f).iterator();
        while (it.hasNext())
        {
            LidarPoint p=it.next();
            double t=p.getTime();
            double x=p.getTargetPosition().getX();
            double y=p.getTargetPosition().getY();
            double z=p.getTargetPosition().getZ();
            tmin=Math.min(tmin, t);
            tmax=Math.max(tmax, t);
            xmin=Math.min(xmin, x);
            xmax=Math.max(xmax, x);
            ymin=Math.min(ymin, y);
            ymax=Math.max(ymax, y);
            zmin=Math.min(zmin, z);
            zmax=Math.max(zmax, z);
        }
    }
}
