package edu.jhuapl.sbmt.lidar;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;

public enum LidarInstrument
{
    OLA("l2", 0, 0, 0, 0, 0, 0, 0, 0),
    NLR("TAB", 5032837.41, 3.527905517E7, -17.93654, 17.839299999999998, -17.66817, 17.586240000000004, -16.82305, 14.81531),
    LASER("csv", 5.850823679999998E8, 5.864394679999999E8,6135.629776000001,6503.644220000001,-13961.141818000002,6520.820436,-15771.308564,6298.68915);


    // new Bennu(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V3 Image"))
    // new Eros(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL))

    static { Configuration.setAPLVersion(true); };

    String rawFileExtension;
    BoundingBox bbox;
    double tmin,tmax;

    private LidarInstrument(String rawFileExtension, double tmin, double tmax, double xmin, double xmax, double ymin, double ymax, double zmin, double zmax)
    {
        this.rawFileExtension=rawFileExtension;
        bbox=new BoundingBox(new double[]{xmin,xmax,ymin,ymax,zmin,zmax});
        this.tmin=tmin;
        this.tmax=tmax;
    }

    public String getRawFileExtension()
    {
        return rawFileExtension;
    }

    public BoundingBox getBoundingBox()
    {
        return bbox;
    }

    public double getTmin()
    {
        return tmin;
    }

    public double getTmax()
    {
        return tmax;
    }

}
