package edu.jhuapl.sbmt.lidar.gui.color;

import java.awt.event.ActionListener;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.gui.ColorBarPanel;
import edu.jhuapl.saavtk.color.gui.EditGroupColorPanel;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.provider.ColorBarColorProvider;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.lidar.LidarFeatureType;
import edu.jhuapl.sbmt.lidar.LidarManager;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Custom {@link ColorBarPanel} that adds support for lidar color configuration.
 *
 * @author lopeznr1
 */
public class LidarColorBarPanel<G1> extends ColorBarPanel implements ItemEventListener, EditGroupColorPanel
{
	// Ref vars
	private final LidarManager<G1> refManager;
	private final Renderer refRenderer;
	private final ColorBarPainter refColorBarPainter;

	/** Standard Constructor */
	public LidarColorBarPanel(ActionListener aListener, LidarManager<G1> aManager, Renderer aRenderer,
			ColorBarPainter aColorBarPainter)
	{
		super(aColorBarPainter, true);

		refManager = aManager;
		refRenderer = aRenderer;
		refColorBarPainter = aColorBarPainter;

		// Register lidar specific FeatureTypes
		addFeatureType(LidarFeatureType.Intensity, "Intensity");
		addFeatureType(LidarFeatureType.Radius, "Radius");
		addFeatureType(LidarFeatureType.Range, "Spacecraft Range");
		addFeatureType(LidarFeatureType.Time, "Time");
		setFeatureType(LidarFeatureType.Range);

		// Auto register the provided ActionListener
		addActionListener(aListener);

		// Register for events of interest
		addActionListener((aEvent) -> updateColorBar());
		refManager.addListener(this);
	}

	@Override
	public void activate(boolean aIsActive)
	{
		// Ensure our default range is in sync
		updateDefaultRange();

		// Force install the ColorMapAttr with the default (reset) range
		ColorMapAttr tmpCMA = getColorMapAttr();

		double minVal = Double.NaN;
		double maxVal = Double.NaN;
		FeatureType tmpFT = getFeatureType();
		Range<Double> tmpRange = getResetRange(tmpFT);
		if (tmpRange != null)
		{
			minVal = tmpRange.lowerEndpoint();
			maxVal = tmpRange.upperEndpoint();
		}

		tmpCMA = new ColorMapAttr(tmpCMA.getColorTable(), minVal, maxVal, tmpCMA.getNumLevels(), tmpCMA.getIsLogScale());
		setColorMapAttr(tmpCMA);

		// Update the color bar
		updateColorBar();

		// Update the renderer to reflect the ColorBarPainter
		if (aIsActive == true)
			refRenderer.addVtkPropProvider(refColorBarPainter);
		else
			refRenderer.delVtkPropProvider(refColorBarPainter);
	}

	@Override
	public GroupColorProvider getGroupColorProvider()
	{
		ColorProvider tmpCP = new ColorBarColorProvider(getColorMapAttr(), getFeatureType());
		return new ConstGroupColorProvider(tmpCP);
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		// Update our default range
		if (aEventType == ItemEventType.ItemsChanged || aEventType == ItemEventType.ItemsMutated)
			updateDefaultRange();
	}

	/**
	 * Helper method to calculate the range of values for the specified
	 * {@link FeatureType}.
	 */
	private Range<Double> calcRangeForFeature(FeatureType aFeatureType)
	{
		Range<Double> fullRange = null;
		for (G1 aItem : refManager.getAllItems())
		{
			// Skip to next if the lidar object is not rendered
			if (refManager.getIsVisible(aItem) == false)
				continue;

			fullRange = updateRange(aItem, aFeatureType, fullRange);
		}

		return fullRange;
	}

	/**
	 * Helper method that updates the default range for all of the lidar
	 * {@link FeatureType}s.
	 */
	private void updateDefaultRange()
	{
		// Bail if we are not visible. Maintenance of default range
		// synchronization is relevant only when the panel is visible.
		if (isShowing() == false)
			return;

		for (FeatureType aFeatureType : LidarFeatureType.FullSet)
		{
			Range<Double> tmpRange = calcRangeForFeature(aFeatureType);
			setResetRange(aFeatureType, tmpRange);
		}
	}

	/**
	 * Helper method that keeps the {@link ColorBarPainter} synchronized with the
	 * gui.
	 */
	private void updateColorBar()
	{
		ColorMapAttr tmpCMA = getColorMapAttr();
		refColorBarPainter.setColorMapAttr(tmpCMA);

		FeatureType tmpFT = getFeatureType();
		refColorBarPainter.setTitle(tmpFT.getName());
	}

	/**
	 * Helper method that will update the fullRangeZ state var to include the
	 * specified lidar data.
	 */
	private Range<Double> updateRange(G1 aItem, FeatureType aFeatureType, Range<Double> aFullRange)
	{
		// Bail if there are no values associated with the feature
		FeatureAttr tmpFA = refManager.getFeatureAttrFor(aItem, aFeatureType);
		if (tmpFA == null || tmpFA.getNumVals() == 0)
			return aFullRange;

		Range<Double> tmpRangeZ = Range.closed(tmpFA.getMinVal(), tmpFA.getMaxVal());

		// Grow fullRangeZ to include the specified lidar data
		if (aFullRange == null)
			aFullRange = tmpRangeZ;
		aFullRange = aFullRange.span(tmpRangeZ);
		return aFullRange;
	}

}
