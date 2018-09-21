package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.sbmt.lidar.RawLidarFile;
import edu.jhuapl.sbmt.util.TimeUtil;


public class Hayabusa2LaserRawLidarFile extends RawLidarFile
{

//    public enum Fields
//    {
//        TIME,
//        PACKET_TI,
//        LASER,
//        RANGE,
//        SIG_FAR,
//        SIG_NEAR,
//        TOPO_LONGITUDE,
//        TOPO_LATITUDE,
//        TOPO_HEIGHT,
//        TOPO_X,
//        TOPO_Y,
//        TOPO_Z,
//        SC_POS_X,
//        SC_POS_Y,
//        SC_POS_Z
//    }

    public enum Fields
    {
        SHOT_TIME,
        RANGE,
        TOPO_LONGITUDE,
        TOPO_LATITUDE,
        TOPO_HEIGHT,
        TOPO_X,
        TOPO_Y,
        TOPO_Z,
        SC_POS_X,
        SC_POS_Y,
        SC_POS_Z
    }

    final double scaleFactor= 1.0e-3;//1e-3;//6.371*2.; //vectors here expect km, but xml specifies coordinates in m, so this converts the meters coming in into km

    public Hayabusa2LaserRawLidarFile(String pathname)
    {
        super(pathname);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void readFully()
    {
        try
        {
            Scanner scanner=new Scanner(this);
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                String[] tokens=line.trim().split(",");
                double time=Double.valueOf(TimeUtil.str2et(tokens[Fields.SHOT_TIME.ordinal()]));  // s
                double tgx=Double.valueOf(tokens[Fields.TOPO_X.ordinal()]);
                double tgy=Double.valueOf(tokens[Fields.TOPO_Y.ordinal()]);
                double tgz=Double.valueOf(tokens[Fields.TOPO_Z.ordinal()]);
                double scx=Double.valueOf(tokens[Fields.SC_POS_X.ordinal()]);
                double scy=Double.valueOf(tokens[Fields.SC_POS_Y.ordinal()]);
                double scz=Double.valueOf(tokens[Fields.SC_POS_Z.ordinal()]);
                double rng=Double.valueOf(tokens[Fields.RANGE.ordinal()]) * scaleFactor;
                Vector3D scpos=new Vector3D(scx,scy,scz).scalarMultiply(scaleFactor);
                Vector3D tgpos=new Vector3D(tgx,tgy,tgz).scalarMultiply(scaleFactor);
                //L2 data doesn't have intensity.  Just going to set it to some arbitrary value
                double intensity = 100.0;
//                double intensity=Double.valueOf(tokens[Fields.SIG_FAR.ordinal()]);
                points.add(new Hayabusa2LaserLidarPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), rng, intensity, getFileNumber()));
            }
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int getFileNumber()
    {
        return getName().hashCode();
    }
}
