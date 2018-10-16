package edu.jhuapl.sbmt.lidar;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;

public enum LidarInstrument
{
    OLA("l2", 6.039524109549538E8, 6.089525489072417E8, -0.37990207076072693, 0.3882920801639557, -0.3699320614337921, 0.37232205867767334, -0.36133206486701965, 0.3657020688056946),
    NLR("TAB", 5032837.41, 3.527905517E7, -17.93654, 17.839299999999998, -17.66817, 17.586240000000004, -16.82305, 14.81531),
    LASER("csv", 5.850823679999998E8,5.864394679999999E8,-481.528,510.41,-1095.679,511.758,-1237.742,494.325);

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
