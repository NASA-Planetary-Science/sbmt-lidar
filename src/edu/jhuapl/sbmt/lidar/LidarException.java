package edu.jhuapl.sbmt.lidar;

/**
 * Exception specific to the lidar package of SBMT.
 *
 * @author lopeznr1
 */
public class LidarException extends RuntimeException
{
	/** Delegate Constructor **/
	public LidarException(String aErrorMsg, Throwable aThrowable)
	{
		super(aErrorMsg, aThrowable);
	}

	/** Delegate Constructor **/
	public LidarException(String aErrorMsg)
	{
		super(aErrorMsg);
	}

}
