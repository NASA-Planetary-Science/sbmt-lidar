package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.sbmt.lidar.BasicLidarPoint;
import edu.jhuapl.sbmt.lidar.LidarPoint;

/**
 * Collection of utility methods used to read a LidarPoint from a stream.
 *
 * @author lopeznr1
 */
public class OlaStreamUtil
{
	/**
	 * Returns the LidarPoint that was read from the stream.
	 * <P>
	 * Returns null if any {@link EOFException} is thrown
	 */
	public static BasicLidarPoint readLidarPointFromStream(DataInputStream aStream) throws IOException
	{
		try
		{
			double time = aStream.readDouble();
			Vector3D tgpos = new Vector3D(aStream.readDouble(), aStream.readDouble(), aStream.readDouble());
			double intensity = aStream.readDouble();
			Vector3D scpos = new Vector3D(aStream.readDouble(), aStream.readDouble(), aStream.readDouble());

			return new BasicLidarPoint(scpos, tgpos, time, 0.0, intensity);
		}
		catch (EOFException aExp)
		{
			; // Nothing to do
		}

		return null;
	}

	/**
	 * Writes a LidarPoint to the specified stream.
	 *
	 * @param aStream
	 * @param aLP
	 * @throws IOException
	 */
	public static void writeLidarPointToStream(DataOutputStream aStream, LidarPoint aLP) throws IOException
	{
		aStream.writeDouble(aLP.getTime());
		aStream.writeDouble(aLP.getTargetPosition().getX());
		aStream.writeDouble(aLP.getTargetPosition().getY());
		aStream.writeDouble(aLP.getTargetPosition().getZ());
		aStream.writeDouble(aLP.getIntensityReceived());
		aStream.writeDouble(aLP.getSourcePosition().getX());
		aStream.writeDouble(aLP.getSourcePosition().getY());
		aStream.writeDouble(aLP.getSourcePosition().getZ());
	}

}
