package edu.jhuapl.sbmt.lidar.test;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface LidarPoint extends Comparable<LidarPoint>
{
    public Vector3D getTargetPosition();
    public Vector3D getSourcePosition();
    public Double getIntensityReceived();
    public Double getTime();
}
