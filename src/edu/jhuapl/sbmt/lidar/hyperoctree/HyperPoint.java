package edu.jhuapl.sbmt.lidar.hyperoctree;

public interface HyperPoint extends Dimensioned
{
    public double getCoordinate(int i);
    public double[] get();
}
