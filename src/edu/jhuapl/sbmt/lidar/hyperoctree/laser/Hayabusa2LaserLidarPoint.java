package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;

public class Hayabusa2LaserLidarPoint extends FSHyperPointWithFileTag
{

    private double range; // range to S/C

    public Hayabusa2LaserLidarPoint()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public Hayabusa2LaserLidarPoint(DataInputStream stream) throws IOException
    {
        super(stream);
        double scaleFactor=1; //6371.*2.;
        data[0]*=scaleFactor;
        data[1]*=scaleFactor;
        data[2]*=scaleFactor;
        data[4]*=scaleFactor;
        data[5]*=scaleFactor;
        data[6]*=scaleFactor;
    }

    public Hayabusa2LaserLidarPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double range, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, intensity, fileNum);
        this.range = range;
    }

}
