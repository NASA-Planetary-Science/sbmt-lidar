package edu.jhuapl.sbmt.lidar.gui.action;

import java.awt.Color;
import java.util.List;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.lidar.LidarManager;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Fixed Color".
 *
 * @author lopeznr1
 */
class FixedLidarColorAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final LidarManager<G1> refManager;
	private final ColorProvider refCP;

	/**
	 * Standard Constructor
	 */
	public FixedLidarColorAction(LidarManager<G1> aManager, Color aColor)
	{
		refManager = aManager;
		refCP = new ConstColorProvider(aColor);
	}

	/**
	 * Returns the color associated with this Action
	 */
	public Color getColor()
	{
		return refCP.getBaseColor();
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		refManager.installCustomColorProviders(aItemL, refCP, refCP);
	}

}