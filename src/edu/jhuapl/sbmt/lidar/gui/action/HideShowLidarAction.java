package edu.jhuapl.sbmt.lidar.gui.action;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.lidar.LidarManager;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Hide/Show Items".
 *
 * @author lopeznr1
 */
class HideShowLidarAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final LidarManager<G1> refManager;

	// Attributes
	private final String itemLabelStr;

	/**
	 * Standard Constructor
	 */
	public HideShowLidarAction(LidarManager<G1> aManager, String aItemLabelStr)
	{
		refManager = aManager;

		itemLabelStr = aItemLabelStr;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Determine if all tracks are shown
		boolean isAllShown = true;
		for (G1 aItem : aItemL)
			isAllShown &= refManager.getIsVisible(aItem) == true;

		// Update the tracks visibility based on whether they are all shown
		boolean tmpBool = isAllShown == false;
		refManager.setIsVisible(aItemL, tmpBool);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Determine if all items are shown
		boolean isAllShown = true;
		for (G1 aItem : aItemC)
			isAllShown &= refManager.getIsVisible(aItem) == true;

		// Determine the display string
		String displayStr = "Hide " + itemLabelStr;
		if (isAllShown == false)
			displayStr = "Show " + itemLabelStr;
		displayStr = MessageUtil.toPluralForm(displayStr, aItemC);

		// Update the text of the associated MenuItem
		aAssocMI.setText(displayStr);

	}
}