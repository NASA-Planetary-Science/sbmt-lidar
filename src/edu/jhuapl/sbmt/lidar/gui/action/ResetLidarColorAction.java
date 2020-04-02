package edu.jhuapl.sbmt.lidar.gui.action;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.lidar.LidarManager;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Reset Colors".
 *
 * @author lopeznr1
 */
class ResetLidarColorAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final LidarManager<G1> refManager;

	/**
	 * Standard Constructor
	 */
	public ResetLidarColorAction(LidarManager<G1> aManager)
	{
		refManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		refManager.clearCustomColorProvider(aItemL);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Determine if any of the lidar colors can be reset
		boolean isResetAvail = false;
		for (G1 aItem : aItemC)
			isResetAvail |= refManager.hasCustomColorProvider(aItem) == true;

		aAssocMI.setEnabled(isResetAvail);
	}
}