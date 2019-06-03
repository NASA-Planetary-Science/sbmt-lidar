package edu.jhuapl.sbmt.lidar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Immutable class that defines a LidarPoint.
 */
public class BasicLidarPoint implements LidarPoint
{
	private final Vector3D scpos;
	private final Vector3D tgpos;
	private final double time;
	private final double intensity;
	private final double rangeToSC;

	public BasicLidarPoint(Vector3D scpos, Vector3D tgpos, double time, double range, double intensity)
	{
		this.scpos = scpos;
		this.tgpos = tgpos;
		this.time = time;
		this.intensity = intensity;
		this.rangeToSC = range;
	}

	public BasicLidarPoint(double[] tgpos, double[] scpos, double time, double range, double intensity)
	{
		this(new Vector3D(scpos), new Vector3D(tgpos), time, range, intensity);
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
	public double getIntensityReceived()
	{
		return intensity;
	}

	@Override
	public double getTime()
	{
		return time;
	}

	@Override
	public int compareTo(LidarPoint o)
	{
		return Double.compare(time, o.getTime());
	}

	@Override
	public double getRangeToSC()
	{
		return rangeToSC;
	}
}
