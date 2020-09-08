package edu.jhuapl.sbmt.lidar.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * ItemHandler used to process lidar Tracks.
 *
 * @author lopeznr1
 */
class TrackItemHandler extends BasicItemHandler<LidarTrack, LookUp>
{
	// Ref vars
	private final LidarTrackManager refManager;

	/**
	 * Standard Constructor
	 */
	public TrackItemHandler(LidarTrackManager aManager, QueryComposer<LookUp> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(LidarTrack aTrack, LookUp aEnum)
	{
		switch (aEnum)
		{
			case IsVisible:
				return refManager.getIsVisible(aTrack);
			case Color:
				return refManager.getColorProviderTarget(aTrack);
			case Name:
				return aTrack.getId();
			case NumPoints:
				return aTrack.getNumberOfPoints();
			case BegTime:
				return aTrack.getTimeBeg();
			case EndTime:
				return aTrack.getTimeEnd();
			case Source:
				return getSourceFileString(aTrack);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(LidarTrack aTrack, LookUp aEnum, Object aValue)
	{
		List<LidarTrack> tmpL = ImmutableList.of(aTrack);

		if (aEnum == LookUp.IsVisible)
			refManager.setIsVisible(tmpL, (boolean) aValue);
		else if (aEnum == LookUp.Color)
		{
			ColorProvider tmpCP = (ColorProvider) aValue;
			refManager.installCustomColorProviders(tmpL, tmpCP, tmpCP);
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 * Utility method that returns the appropriate "Source Files" string for the
	 * specified track.
	 */
	public static String getSourceFileString(LidarTrack aTrack)
	{
		List<String> sourceL = aTrack.getSourceList();
		if (sourceL.size() == 0)
			return "";

		StringBuffer tmpSB = new StringBuffer();
		for (String aSource : sourceL)
		{
			tmpSB.append(" | " + aSource);
			if (tmpSB.length() > 1000)
			{
				tmpSB.append("...");
				break;
			}
		}
		tmpSB.delete(0, 3);

		return tmpSB.toString();
	}

}
