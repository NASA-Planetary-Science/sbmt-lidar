package edu.jhuapl.sbmt.lidar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface LidarPoint extends Comparable<LidarPoint>
{
	public Vector3D getTargetPosition();

	public Vector3D getSourcePosition();

	public double getIntensityReceived();

	public double getTime();

	public double getRangeToSC();
}
