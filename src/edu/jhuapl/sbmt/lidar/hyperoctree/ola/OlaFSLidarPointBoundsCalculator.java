package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSLidarPointBoundsCalculator;

public class OlaFSLidarPointBoundsCalculator extends FSLidarPointBoundsCalculator
{
    public OlaFSLidarPointBoundsCalculator(String inputDirectoryList)
    {
        super(inputDirectoryList,"l2");
    }

    public void checkBounds(File f)
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
