package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSLidarHyperPoint;

public class MolaFSHyperPoint extends FSLidarHyperPoint
{

    public MolaFSHyperPoint()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public MolaFSHyperPoint(DataInputStream stream) throws IOException
    {
        super(stream);
        // TODO Auto-generated constructor stub
    }

    public MolaFSHyperPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, intensity, fileNum);
        // TODO Auto-generated constructor stub
    }

}
