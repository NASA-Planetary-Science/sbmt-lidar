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

	// State vars
	private ColorBarPainter workCBP;

	/** Standard Constructor */
	public LidarColorBarPanel(ActionListener aListener, LidarManager<G1> aManager, Renderer aRenderer)
	{
		refManager = aManager;
		refRenderer = aRenderer;

		workCBP = new ColorBarPainter(refRenderer);

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

		// Reset the current range to the defaults
		double tmpMin = getDefaultMinValue();
		double tmpMax = getDefaultMaxValue();
		setCurrentMinMax(tmpMin, tmpMax);

		// Force an update to the color map
		updateColorMapArea();

		// Update the color bar
		updateColorBar();

		// Update the renderer to reflect the ColorBarPainter
		if (aIsActive == true)
			refRenderer.addVtkPropProvider(workCBP);
		else
			refRenderer.delVtkPropProvider(workCBP);
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

	@Override
	protected void updateDefaultRange()
	{
		// Bail if we are not visible. Maintenance of default range
		// synchronization is relevant only when the panel is visible.
		if (isShowing() == false)
			return;

		FeatureType tmpFT = getFeatureType();
		Range<Double> fullRange = null;
		for (G1 aItem : refManager.getAllItems())
		{
			// Skip to next if the lidar object is not rendered
			if (refManager.getIsVisible(aItem) == false)
				continue;

			fullRange = updateRange(aItem, tmpFT, fullRange);
		}

		// Update our (internal) default range
		double minVal = Double.NaN;
		double maxVal = Double.NaN;
		if (fullRange != null)
		{
			minVal = fullRange.lowerEndpoint();
			maxVal = fullRange.upperEndpoint();
		}

		setDefaultRange(minVal, maxVal);
	}

	/**
	 * Helper method that keeps the {@link ColorBarPainter} synchronized with the
	 * gui.
	 */
	private void updateColorBar()
	{
		ColorMapAttr tmpCMA = getColorMapAttr();
		workCBP.setColorMapAttr(tmpCMA);

		FeatureType tmpFT = getFeatureType();
		workCBP.setTitle(tmpFT.getName());
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
