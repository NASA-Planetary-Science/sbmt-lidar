package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;

public class Hayabusa2LaserLidarPoint extends FSHyperPointWithFileTag
{
    public Hayabusa2LaserLidarPoint()
    {
        super();
    }

    public Hayabusa2LaserLidarPoint(DataInputStream stream) throws IOException
    {
        super(stream);
    }

    public Hayabusa2LaserLidarPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double range, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, range, intensity, fileNum);
    }

    public static Hayabusa2LaserLidarPoint wrap(Hayabusa2LaserLidarPoint pt, int filenum)
    {
        return new Hayabusa2LaserLidarPoint(pt.getTargetPosition().getX(),pt.getTargetPosition().getY(),pt.getTargetPosition().getZ(),pt.getTime(),pt.getSourcePosition().getX(),pt.getSourcePosition().getY(),pt.getSourcePosition().getZ(), pt.getRangeToSC(), pt.getIntensityReceived(),filenum);
    }

}
