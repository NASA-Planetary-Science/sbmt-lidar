package edu.jhuapl.sbmt.lidar.gui.color;

/**
 * Immutable class that defines the attributes associated with a ColorMap.
 *
 * @author lopeznr1
 */
public class ColorMapAttr
{
	// Attributes
	private final String name;
	private final double minVal;
	private final double maxVal;
	private final int numLevels;
	private final boolean isLogScale;

	/**
	 * Standard Constructor
	 */
	public ColorMapAttr(String aName, double aMinVal, double aMaxVal, int aNumLevels, boolean aIsLogScale)
	{
		name = aName;
		minVal = aMinVal;
		maxVal = aMaxVal;
		numLevels = aNumLevels;
		isLogScale = aIsLogScale;
	}

	public String getName()
	{
		return name;
	}

	public double getMinVal()
	{
		return minVal;
	}

	public double getMaxVal()
	{
		return maxVal;
	}

	public int getNumLevels()
	{
		return numLevels;
	}

	public boolean getIsLogScale()
	{
		return isLogScale;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorMapAttr other = (ColorMapAttr) obj;
		if (isLogScale != other.isLogScale)
			return false;
		if (Double.doubleToLongBits(maxVal) != Double.doubleToLongBits(other.maxVal))
			return false;
		if (Double.doubleToLongBits(minVal) != Double.doubleToLongBits(other.minVal))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (numLevels != other.numLevels)
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (isLogScale ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(maxVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + numLevels;
		return result;
	}

}
