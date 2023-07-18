package edu.jhuapl.sbmt.lidar.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.IFeatureConfig;
import edu.jhuapl.sbmt.core.config.Instrument;

public class LidarInstrumentConfig implements IFeatureConfig
{
	public boolean hasHypertreeBasedLidarSearch = false;
	// if hasLidarData is true, the following must be filled in
	public Map<String, String> lidarSearchDataSourceMap = Maps.newHashMap();
	public Map<String, String> lidarBrowseDataSourceMap = Maps.newHashMap(); // overrides
																				// lidarBrowseFileListResourcePath for
																				// OLA
	public Map<String, String> lidarBrowseWithPointsDataSourceMap = Maps.newHashMap();
	public Map<String, ArrayList<Date>> lidarSearchDataSourceTimeMap = Maps.newHashMap();
	public Map<String, ArrayList<Date>> orexSearchTimeMap = Maps.newHashMap();

	// Required if hasLidarData is true:
	public String lidarBrowseOrigPathRegex; // regular expression to match path prefix from database, which may not be
											// current path. May be null to skip regex.
	public String lidarBrowsePathTop; // current top-of-path for lidar data; replaces the expression given by
										// lidarBrowseOrigPathRegex.

	public int[] lidarBrowseXYZIndices = new int[] {};
	public int[] lidarBrowseSpacecraftIndices = new int[] {};
	public int lidarBrowseOutgoingIntensityIndex;
	public int lidarBrowseReceivedIntensityIndex;
	public int lidarBrowseRangeIndex;
	public boolean lidarBrowseIsRangeExplicitInData = false;
	public boolean lidarBrowseIntensityEnabled = false;
	public boolean lidarBrowseIsLidarInSphericalCoordinates = false;
	public boolean lidarBrowseIsSpacecraftInSphericalCoordinates = false;
	public boolean lidarBrowseIsTimeInET = false;
	public int lidarBrowseTimeIndex;
	public int lidarBrowseNoiseIndex;
	public String lidarBrowseFileListResourcePath;
	public int lidarBrowseNumberHeaderLines;
	public boolean lidarBrowseIsBinary = false;
	public int lidarBrowseBinaryRecordSize; // only required if lidarBrowseIsBinary is true

	// Return whether or not the units of the lidar points are in meters. If false
	// they are assumed to be in kilometers.
	public boolean lidarBrowseIsInMeters;
	public double lidarOffsetScale;

	public boolean hasLidarData = false;
	public Date lidarSearchDefaultStartDate;
	public Date lidarSearchDefaultEndDate;

	public Instrument lidarInstrumentName = Instrument.LIDAR;
	
    private BodyViewConfig config;
    
    public LidarInstrumentConfig(BodyViewConfig config)
	{
		this.config = config;
	}
	
	public void setConfig(BodyViewConfig config)
	{
		this.config = config;
	}


//	public Instrument getLidarInstrument();
//
//	public boolean hasHypertreeLidarSearch();

//    @Override
    public Instrument getLidarInstrument()
    {
        return lidarInstrumentName;
    }

    public boolean hasHypertreeLidarSearch()
    {
        return hasHypertreeBasedLidarSearch;
    }

	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		LidarInstrumentConfig c = (LidarInstrumentConfig)super.clone();
		if (this.hasLidarData)
			c.lidarInstrumentName = this.lidarInstrumentName;

		c.hasLidarData = this.hasLidarData;

		if (this.hasLidarData)
		{
			c.lidarSearchDefaultStartDate = (Date) this.lidarSearchDefaultStartDate.clone();
			c.lidarSearchDefaultEndDate = (Date) this.lidarSearchDefaultEndDate.clone();
			c.lidarSearchDataSourceMap = new LinkedHashMap<>(this.lidarSearchDataSourceMap);
			c.lidarBrowseDataSourceMap = new LinkedHashMap<>(this.lidarBrowseDataSourceMap);
			c.lidarBrowseWithPointsDataSourceMap = new LinkedHashMap<>(this.lidarBrowseWithPointsDataSourceMap);
			c.lidarBrowseXYZIndices = this.lidarBrowseXYZIndices.clone();
			c.lidarBrowseSpacecraftIndices = this.lidarBrowseSpacecraftIndices.clone();
			c.lidarBrowseIsLidarInSphericalCoordinates = this.lidarBrowseIsLidarInSphericalCoordinates;
			c.lidarBrowseIsSpacecraftInSphericalCoordinates = this.lidarBrowseIsSpacecraftInSphericalCoordinates;
			c.lidarBrowseIsTimeInET = this.lidarBrowseIsTimeInET;
			c.lidarBrowseTimeIndex = this.lidarBrowseTimeIndex;
			c.lidarBrowseNoiseIndex = this.lidarBrowseNoiseIndex;
			c.lidarBrowseOutgoingIntensityIndex = this.lidarBrowseOutgoingIntensityIndex;
			c.lidarBrowseReceivedIntensityIndex = this.lidarBrowseReceivedIntensityIndex;
			c.lidarBrowseRangeIndex = this.lidarBrowseRangeIndex;
			c.lidarBrowseIsRangeExplicitInData = this.lidarBrowseIsRangeExplicitInData;
			c.lidarBrowseIntensityEnabled = this.lidarBrowseIntensityEnabled;
			c.lidarBrowseFileListResourcePath = this.lidarBrowseFileListResourcePath;
			c.lidarBrowseNumberHeaderLines = this.lidarBrowseNumberHeaderLines;
			c.lidarBrowseIsInMeters = this.lidarBrowseIsInMeters;
			c.lidarBrowseIsBinary = this.lidarBrowseIsBinary;
			c.lidarBrowseBinaryRecordSize = this.lidarBrowseBinaryRecordSize;
			c.lidarOffsetScale = this.lidarOffsetScale;
		}
		return clone();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		result = prime * result + (hasHypertreeBasedLidarSearch ? 1231 : 1237);
		result = prime * result + (hasLidarData ? 1231 : 1237);
		result = prime * result + lidarBrowseBinaryRecordSize;
		result = prime * result + ((lidarBrowseDataSourceMap == null) ? 0 : lidarBrowseDataSourceMap.hashCode());
		result = prime * result + ((lidarBrowseWithPointsDataSourceMap == null) ? 0 : lidarBrowseWithPointsDataSourceMap.hashCode());
		result = prime * result
				+ ((lidarBrowseFileListResourcePath == null) ? 0 : lidarBrowseFileListResourcePath.hashCode());
		result = prime * result + (lidarBrowseIntensityEnabled ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsBinary ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsInMeters ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsLidarInSphericalCoordinates ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsRangeExplicitInData ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsSpacecraftInSphericalCoordinates ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsTimeInET ? 1231 : 1237);
		result = prime * result + lidarBrowseNoiseIndex;
		result = prime * result + lidarBrowseNumberHeaderLines;
		result = prime * result + ((lidarBrowseOrigPathRegex == null) ? 0 : lidarBrowseOrigPathRegex.hashCode());
		result = prime * result + lidarBrowseOutgoingIntensityIndex;
		result = prime * result + ((lidarBrowsePathTop == null) ? 0 : lidarBrowsePathTop.hashCode());
		result = prime * result + lidarBrowseRangeIndex;
		result = prime * result + lidarBrowseReceivedIntensityIndex;
		result = prime * result + Arrays.hashCode(lidarBrowseSpacecraftIndices);
		result = prime * result + lidarBrowseTimeIndex;
		result = prime * result + Arrays.hashCode(lidarBrowseXYZIndices);
		result = prime * result + ((lidarInstrumentName == null) ? 0 : lidarInstrumentName.hashCode());
		temp = Double.doubleToLongBits(lidarOffsetScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((lidarSearchDataSourceMap == null) ? 0 : lidarSearchDataSourceMap.hashCode());
		result = prime * result
				+ ((lidarSearchDataSourceTimeMap == null) ? 0 : lidarSearchDataSourceTimeMap.hashCode());
		result = prime * result + ((lidarSearchDefaultEndDate == null) ? 0 : lidarSearchDefaultEndDate.hashCode());
		result = prime * result + ((lidarSearchDefaultStartDate == null) ? 0 : lidarSearchDefaultStartDate.hashCode());


		return result;
	}

	@Override
    public boolean equals(Object obj)
    {
		if (this == obj)
			return true;
		if (!super.equals(obj))
		{
			return false;
		}

		LidarInstrumentConfig other = (LidarInstrumentConfig) obj;

		if (hasHypertreeBasedLidarSearch != other.hasHypertreeBasedLidarSearch)
		{
			return false;
		}
		if (hasLidarData != other.hasLidarData)
		{
			return false;
		}

		if (lidarBrowseBinaryRecordSize != other.lidarBrowseBinaryRecordSize)
		{
//			System.err.println("BodyViewConfig: equals: lidar browse binary record size doesn't match " + lidarBrowseBinaryRecordSize + " " + other.lidarBrowseBinaryRecordSize);
			return false;
		}
		if (lidarBrowseDataSourceMap == null)
		{
			if (other.lidarBrowseDataSourceMap != null)
			{
				return false;
			}
		} else if (!lidarBrowseDataSourceMap.equals(other.lidarBrowseDataSourceMap))
		{
			return false;
		}
		if (lidarBrowseWithPointsDataSourceMap == null)
		{
			if (other.lidarBrowseWithPointsDataSourceMap != null)
			{
				return false;
			}
		} else if (!lidarBrowseWithPointsDataSourceMap.equals(other.lidarBrowseWithPointsDataSourceMap))
		{
			return false;
		}
		if (lidarBrowseFileListResourcePath == null)
		{
			if (other.lidarBrowseFileListResourcePath != null)
			{
				return false;
			}
		}
		else if (!lidarBrowseFileListResourcePath.equals(other.lidarBrowseFileListResourcePath))
		{
			return false;
		}
		if (lidarBrowseIntensityEnabled != other.lidarBrowseIntensityEnabled)
		{
//			System.err.println("BodyViewConfig: equals: browse intensity enabled don't match ");
			return false;
		}
		if (lidarBrowseIsBinary != other.lidarBrowseIsBinary)
		{
			return false;
		}
		if (lidarBrowseIsInMeters != other.lidarBrowseIsInMeters)
		{
			return false;
		}
		if (lidarBrowseIsLidarInSphericalCoordinates != other.lidarBrowseIsLidarInSphericalCoordinates)
		{
//			System.err.println("BodyViewConfig: equals: is lidar in sph coords unequal " + lidarBrowseIsLidarInSphericalCoordinates + " " + other.lidarBrowseIsLidarInSphericalCoordinates);
			return false;
		}
		if (lidarBrowseIsRangeExplicitInData != other.lidarBrowseIsRangeExplicitInData)
		{
//			System.err.println("BodyViewConfig: equals: is lidar range explicit unequal");
			return false;
		}
		if (lidarBrowseIsSpacecraftInSphericalCoordinates != other.lidarBrowseIsSpacecraftInSphericalCoordinates)
		{
//			System.err.println("BodyViewConfig: equals: lidar is sc in sph coords unequal");
			return false;
		}
		if (lidarBrowseIsTimeInET != other.lidarBrowseIsTimeInET)
		{
//			System.err.println("BodyViewConfig: equals: lidar browse in ET Doesn't match");
			return false;
		}
		if (lidarBrowseNoiseIndex != other.lidarBrowseNoiseIndex)
		{
			return false;
		}
		if (lidarBrowseNumberHeaderLines != other.lidarBrowseNumberHeaderLines)
		{
			return false;
		}
		if (lidarBrowseOrigPathRegex == null)
		{
			if (other.lidarBrowseOrigPathRegex != null)
				return false;
		} else if (!lidarBrowseOrigPathRegex.equals(other.lidarBrowseOrigPathRegex))
		{
			return false;
		}
		if (lidarBrowseOutgoingIntensityIndex != other.lidarBrowseOutgoingIntensityIndex)
		{
			return false;
		}
		if (lidarBrowsePathTop == null)
		{
			if (other.lidarBrowsePathTop != null)
				return false;
		} else if (!lidarBrowsePathTop.equals(other.lidarBrowsePathTop))
		{
			return false;
		}
		if (lidarBrowseRangeIndex != other.lidarBrowseRangeIndex)
		{
			return false;
		}
		if (lidarBrowseReceivedIntensityIndex != other.lidarBrowseReceivedIntensityIndex)
		{
			return false;
		}
		if (!Arrays.equals(lidarBrowseSpacecraftIndices, other.lidarBrowseSpacecraftIndices))
		{
			return false;
		}
		if (lidarBrowseTimeIndex != other.lidarBrowseTimeIndex)
		{
			return false;
		}
		if (!Arrays.equals(lidarBrowseXYZIndices, other.lidarBrowseXYZIndices))
		{
			return false;
		}
		if (lidarInstrumentName != other.lidarInstrumentName)
		{
			return false;
		}
		if (Double.doubleToLongBits(lidarOffsetScale) != Double.doubleToLongBits(other.lidarOffsetScale))
		{
			return false;
		}
		if (lidarSearchDataSourceMap == null)
		{
			if (other.lidarSearchDataSourceMap != null)
				return false;
		} else if (!lidarSearchDataSourceMap.equals(other.lidarSearchDataSourceMap))
			return false;
		if (lidarSearchDataSourceTimeMap == null)
		{
			if (other.lidarSearchDataSourceTimeMap != null)
				return false;
		} else if (!lidarSearchDataSourceTimeMap.equals(other.lidarSearchDataSourceTimeMap))
			return false;
		if (lidarSearchDefaultEndDate == null)
		{
			if (other.lidarSearchDefaultEndDate != null)
				return false;
		} else if (!lidarSearchDefaultEndDate.equals(other.lidarSearchDefaultEndDate))
			return false;
		if (lidarSearchDefaultStartDate == null)
		{
			if (other.lidarSearchDefaultStartDate != null)
				return false;
		} else if (!lidarSearchDefaultStartDate.equals(other.lidarSearchDefaultStartDate))
			return false;


		return true;
    }

}
