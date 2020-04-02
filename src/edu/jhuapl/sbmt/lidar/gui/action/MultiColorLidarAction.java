package edu.jhuapl.sbmt.lidar.gui.action;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.lidar.LidarManager;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} corresponding to the lidar color menu item. This action
 * does not provide any color changing function but rather delegate to sub
 * actions.
 *
 * @author lopeznr1
 */
public class MultiColorLidarAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final LidarManager<G1> refManager;

	// State vars
	private Map<JMenuItem, PopAction<G1>> actionM;

	/**
	 * Standard Constructor
	 */
	public MultiColorLidarAction(LidarManager<G1> aManager, Component aParent, JMenu aMenu)
	{
		refManager = aManager;

		actionM = new HashMap<>();

		// Form the static color menu items
		for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
		{
			PopAction<G1> tmpLPA = new FixedLidarColorAction<>(aManager, color.color());
			JCheckBoxMenuItem tmpColorMI = new JCheckBoxMenuItem(tmpLPA);
			tmpColorMI.setText(color.toString().toLowerCase().replace('_', ' '));
			actionM.put(tmpColorMI, tmpLPA);

			aMenu.add(tmpColorMI);
		}
		aMenu.addSeparator();

		JMenuItem customColorMI = formMenuItem(new CustomLidarColorAction<>(aManager, aParent), "Custom...");
		aMenu.add(customColorMI);
		aMenu.addSeparator();

		JMenuItem resetColorMI = formMenuItem(new ResetLidarColorAction<>(aManager), "Reset");
		aMenu.add(resetColorMI);
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		; // Nothing to do
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Determine if all selected items have the same (custom) color
		Color initColor = refManager.getColorProviderTarget(aItemC.iterator().next()).getBaseColor();
		boolean isSameCustomColor = true;
		for (G1 aItem : aItemC)
		{
			Color evalColor = refManager.getColorProviderTarget(aItem).getBaseColor();
			isSameCustomColor &= Objects.equals(initColor, evalColor) == true;
			isSameCustomColor &= refManager.hasCustomColorProvider(aItem) == true;
		}

		// Update our child LidarPopActions
		for (JMenuItem aMI : actionM.keySet())
		{
			PopAction<G1> tmpLPA = actionM.get(aMI);
			tmpLPA.setChosenItems(aItemC, aMI);

			// If all items have the same custom color and match one of the
			// predefined colors then update the corresponding menu item.
			if (tmpLPA instanceof FixedLidarColorAction<?>)
			{
				boolean isSelected = isSameCustomColor == true;
				isSelected &= ((FixedLidarColorAction<?>) tmpLPA).getColor().equals(initColor) == true;
				aMI.setSelected(isSelected);
			}
		}
	}

	/**
	 * Helper method to form and return the specified menu item.
	 * <P>
	 * The menu item will be registered into the action map.
	 *
	 * @param aAction Action corresponding to the menu item.
	 * @param aTitle The title of the menu item.
	 */
	private JMenuItem formMenuItem(PopAction<G1> aAction, String aTitle)
	{
		JMenuItem retMI = new JMenuItem(aAction);
		retMI.setText(aTitle);

		actionM.put(retMI, aAction);

		return retMI;
	}

}
