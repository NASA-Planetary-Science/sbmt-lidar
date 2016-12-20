package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MathUtil;

public class OlaBruteForceBoundsCalculator
{
    static double tmin=Double.POSITIVE_INFINITY;
    static double tmax=Double.NEGATIVE_INFINITY;
    static double xmin=Double.POSITIVE_INFINITY;
    static double xmax=Double.NEGATIVE_INFINITY;
    static double ymin=Double.POSITIVE_INFINITY;
    static double ymax=Double.NEGATIVE_INFINITY;
    static double zmin=Double.POSITIVE_INFINITY;
    static double zmax=Double.NEGATIVE_INFINITY;

    public static void main(String[] args) throws IOException
    {
        String inputDirectoryListFileString=args[0];
        System.out.println("Input data directory listing = "+inputDirectoryListFileString);
        Path inputDirectoryListFile=Paths.get(inputDirectoryListFileString);
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

        System.out.println("Processing files...");
        for (int i=0; i<fileList.size(); i++)
        {
            getBounds(fileList.get(i));
            System.out.println("File "+i+"/"+fileList.size()+":  tmin="+tmin+" tmax="+tmax+"  xmin="+xmin+" xmax="+xmax+"  ymin="+ymin+" ymax="+ymax+"  zmin="+zmin+" zmax="+zmax);
        }

        System.out.println("Final results:  tmin="+tmin+" tmax="+tmax+"  xmin="+xmin+" xmax="+xmax+"  ymin="+ymin+" ymax="+ymax+"  zmin="+zmin+" zmax="+zmax);

    }

    public static void getBounds(File f)
    {
        try
        {
            String filePathString=f.toString();
            if (!filePathString.endsWith(".l2"))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected .l2");

            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));

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
                    if (time>tmax)
                        tmax=time;
                    if (time<tmin)
                        tmin=time;
                    double tgx=tgpos.getX();
                    double tgy=tgpos.getY();
                    double tgz=tgpos.getZ();
                    if (tgx>xmax)
                        xmax=tgx;
                    if (tgx<xmin)
                        xmin=tgx;
                    if (tgy>ymax)
                        ymax=tgy;
                    if (tgy<ymin)
                        ymin=tgy;
                    if (tgz>zmax)
                        zmax=tgz;
                    if (tgz<zmin)
                        zmin=tgz;
                }
            }
            in.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
