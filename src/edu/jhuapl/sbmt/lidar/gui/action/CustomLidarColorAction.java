package edu.jhuapl.sbmt.lidar.gui.action;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.sbmt.lidar.LidarManager;
import edu.jhuapl.sbmt.lidar.gui.color.ColorProvider;
import edu.jhuapl.sbmt.lidar.gui.color.ConstColorProvider;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Custom Color".
 *
 * @author lopeznr1
 */
class CustomLidarColorAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final LidarManager<G1> refManager;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public CustomLidarColorAction(LidarManager<G1> aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		Color tmpColor = refManager.getColorProviderTarget(aItemL.get(0)).getBaseColor();
		Color newColor = ColorChooser.showColorChooser(refParent, tmpColor);
		if (newColor == null)
			return;

		ColorProvider tmpCP = new ConstColorProvider(newColor);
		refManager.installCustomColorProviders(aItemL, tmpCP, tmpCP);
	}
}