package edu.jhuapl.sbmt.lidar.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.lidar.gui.color.ColorProvider;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * ItemHandler used to process LidarFileSpecs.
 *
 * @author lopeznr1
 */
class FileSpecItemHandler extends BasicItemHandler<LidarFileSpec, LookUp>
{
	// Ref vars
	private final LidarFileSpecManager refManager;

	/**
	 * Standard Constructor
	 */
	public FileSpecItemHandler(LidarFileSpecManager aManager, QueryComposer<LookUp> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(LidarFileSpec aFileSpec, LookUp aEnum)
	{
		switch (aEnum)
		{
			case IsVisible:
				return refManager.getIsVisible(aFileSpec);
			case Color:
				if (refManager.isLoaded(aFileSpec) == false)
					return null;
				return refManager.getColorProviderTarget(aFileSpec);
			case NumPoints:
				return refManager.getNumberOfPoints(aFileSpec);
			case Name:
				return aFileSpec.getName();
			case BegTime:
				return aFileSpec.getTimeBeg();
			case EndTime:
				return aFileSpec.getTimeEnd();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(LidarFileSpec aFileSpec, LookUp aEnum, Object aValue)
	{
		List<LidarFileSpec> tmpL = ImmutableList.of(aFileSpec);
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

}
