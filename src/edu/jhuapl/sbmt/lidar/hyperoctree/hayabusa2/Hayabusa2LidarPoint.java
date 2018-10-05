package edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;

public class Hayabusa2LidarPoint extends FSHyperPointWithFileTag
{
    public Hayabusa2LidarPoint()
    {
        super();
    }

    public Hayabusa2LidarPoint(DataInputStream stream) throws IOException
    {
        super(stream);
    }

    public Hayabusa2LidarPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double range, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, range, intensity, fileNum);
    }

    public static Hayabusa2LidarPoint wrap(LidarPoint pt, int filenum)
    {
        return new Hayabusa2LidarPoint(pt.getTargetPosition().getX(),pt.getTargetPosition().getY(),pt.getTargetPosition().getZ(),pt.getTime(),pt.getSourcePosition().getX(),pt.getSourcePosition().getY(),pt.getSourcePosition().getZ(), pt.getRangeToSC(), pt.getIntensityReceived(),filenum);
    }

    @Override
    public int getDimension()
    {
        return 5;
    }

    @Override
    public double getCoordinate(int i)
    {
        // return the coordinate for x, y, z, time, range (ordered as such in hyperbox)
        if (i == 4) // range is actually data[8]
            return data[8];
        else
            return data[i]; // 0-3 are x, y, z, time.
    }

}
