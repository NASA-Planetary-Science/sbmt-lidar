package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.collect.Lists;

public abstract class FSLidarPointBoundsCalculator
{
    public double tmin=Double.POSITIVE_INFINITY;
    public double tmax=Double.NEGATIVE_INFINITY;
    public double xmin=Double.POSITIVE_INFINITY;
    public double xmax=Double.NEGATIVE_INFINITY;
    public double ymin=Double.POSITIVE_INFINITY;
    public double ymax=Double.NEGATIVE_INFINITY;
    public double zmin=Double.POSITIVE_INFINITY;
    public double zmax=Double.NEGATIVE_INFINITY;


    public FSLidarPointBoundsCalculator(String inputDirectoryList, String extension)
    {
        List<File> fileList=Lists.newArrayList();
        try
        {
            System.out.println("Input data directory listing = "+inputDirectoryList);
            Path inputDirectoryListFile=Paths.get(inputDirectoryList);
            Scanner scanner=new Scanner(inputDirectoryListFile.toFile());
            while (scanner.hasNextLine())
            {
                File dataDirectory=inputDirectoryListFile.getParent().resolve(scanner.nextLine().trim()).toFile();
                System.out.println("Searching for ."+extension+" files in "+dataDirectory.toString());
                Collection<File> fileCollection=FileUtils.listFiles(dataDirectory, new WildcardFileFilter("*."+extension), null);
                for (File f : fileCollection) {
                    System.out.println("Adding file "+f+" to the bounds processing queue");
                    fileList.add(f);
                }
            }
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Processing files...");
        for (int i=0; i<fileList.size(); i++)
        {
            checkBounds(fileList.get(i));
            //System.out.println("File "+i+"/"+fileList.size()+":  tmin="+tmin+" tmax="+tmax+"  xmin="+xmin+" xmax="+xmax+"  ymin="+ymin+" ymax="+ymax+"  zmin="+zmin+" zmax="+zmax);
        }

        //System.out.println("Final results:  tmin="+tmin+" tmax="+tmax+"  xmin="+xmin+" xmax="+xmax+"  ymin="+ymin+" ymax="+ymax+"  zmin="+zmin+" zmax="+zmax);

    }

    public abstract void checkBounds(File f);

}
