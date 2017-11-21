package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;

public class LaserLidarPoint extends FSHyperPointWithFileTag
{

    public LaserLidarPoint()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public LaserLidarPoint(DataInputStream stream) throws IOException
    {
        super(stream);
        double scaleFactor=6371.*2.;
        data[0]*=scaleFactor;
        data[1]*=scaleFactor;
        data[2]*=scaleFactor;
        data[4]*=scaleFactor;
        data[5]*=scaleFactor;
        data[6]*=scaleFactor;
    }

    public LaserLidarPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, intensity, fileNum);
        // TODO Auto-generated constructor stub
    }

}
