package edu.jhuapl.sbmt.lidar.gui.color;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * ListCellRenderer used to render custom labels corresponding to the provided
 * items.
 * <P>
 * If an item is not registered via {@link #addMapping(Object, String)}, then
 * the string returned by {@link Object#toString()} will be utilized instead.
 *
 * @author lopeznr1
 */
public class CustomListCellRenderer extends DefaultListCellRenderer
{
	// State vars
	private final Map<Object, String> labelM;

	/**
	 * Standard Constructor
	 */
	public CustomListCellRenderer()
	{
		labelM = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object aObj, int index, boolean isSelected,
			boolean hasFocus)
	{
		JLabel retL = (JLabel) super.getListCellRendererComponent(list, aObj, index, isSelected, hasFocus);

		String tmpLabel = labelM.get(aObj);
		if (tmpLabel == null)
			tmpLabel = "" + aObj;

		retL.setText(tmpLabel);
		return retL;
	}

	/**
	 * Registers the label that will be shown when the specified item is
	 * selected.
	 */
	public void addMapping(Object aItem, String aLabel)
	{
		labelM.put(aItem, aLabel);
	}

	/**
	 * Deregisters the label that will be shown when the specified item is
	 * selected.
	 */
	public void delMapping(Object aItem)
	{
		labelM.remove(aItem);
	}

	/**
	 * Returns the label associated with the specified mapping.
	 */
	public String getLabel(Object aItem)
	{
		return labelM.get(aItem);
	}

}
