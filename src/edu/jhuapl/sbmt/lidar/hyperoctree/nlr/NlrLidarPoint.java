package edu.jhuapl.sbmt.lidar.hyperoctree.nlr;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;


public class NlrLidarPoint extends FSHyperPointWithFileTag
{

    public NlrLidarPoint()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public NlrLidarPoint(DataInputStream stream) throws IOException
    {
        super(stream);
        // TODO Auto-generated constructor stub
    }

    public NlrLidarPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double range, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, range, intensity, fileNum);
        // TODO Auto-generated constructor stub
    }

}
