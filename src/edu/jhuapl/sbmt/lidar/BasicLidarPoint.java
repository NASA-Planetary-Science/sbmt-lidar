package edu.jhuapl.sbmt.lidar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class BasicLidarPoint implements LidarPoint
{
    protected Vector3D scpos;
    protected Vector3D tgpos;
    protected Double time;
    protected Double intensity;

    public BasicLidarPoint(Vector3D scpos, Vector3D tgpos, double time, double intensity)
    {
        this.scpos=scpos;
        this.tgpos=tgpos;
        this.time=time;
        this.intensity=intensity;
    }

    public BasicLidarPoint(double[] tgpos, double[] scpos, double time, double intensity)
    {
        this.scpos=new Vector3D(scpos);
        this.tgpos=new Vector3D(tgpos);
        this.time=time;
        this.intensity=new Double(intensity);
    }

    public BasicLidarPoint(double[] tgpos, double[] scpos, double time)
    {
        this.scpos=new Vector3D(scpos);
        this.tgpos=new Vector3D(tgpos);
        this.time=time;
        this.intensity=new Double(0);
    }

    @Override
    public Vector3D getTargetPosition()
    {
        return tgpos;
    }

    @Override
    public Vector3D getSourcePosition()
    {
        return scpos;
    }

    @Override
    public Double getIntensityReceived()
    {
        return intensity;
    }

    @Override
    public Double getTime()
    {
        return time;
    }

    @Override
    public int compareTo(LidarPoint o)
    {
        return time.compareTo(o.getTime());
    }
}
