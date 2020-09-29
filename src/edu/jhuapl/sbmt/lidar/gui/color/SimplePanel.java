package edu.jhuapl.sbmt.lidar.gui.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.gui.EditGroupColorPanel;
import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.provider.SimpleColorProvider;
import edu.jhuapl.saavtk.util.ColorIcon;

import glum.gui.GuiUtil;
import net.miginfocom.swing.MigLayout;

/**
 * {@link EditGroupColorPanel} that provides simplistic configuration of lidar
 * data.
 * <P>
 * Please note that the method {@link #getGroupColorProvider()} should never be
 * called but rather the more specific {@link #getGroupColorProviderSource()} or
 * {@link #getGroupColorProviderTarget()}. Calling
 * {@link #getGroupColorProvider()} will result in a {@link RuntimeException}.
 * <P>
 * The configuration options are:
 * <UL>
 * <LI>A single color for all source lidar data points (spacecraft).
 * <LI>A single color for all target lidar data points (ground).
 * </UL>
 *
 * @author lopeznr1
 */
public class SimplePanel extends JPanel implements ActionListener, EditGroupColorPanel
{
	// Reference vars
	private final ActionListener refListener;

	// Gui vars
	private JLabel srcColorL, tgtColorL;
	private JButton srcColorB, tgtColorB;

	// State vars
	private Color srcColor;
	private Color tgtColor;

	/** Standard Constructor */
	public SimplePanel(ActionListener aListener, Color aSrcColor, Color aTgtColor)
	{
		refListener = aListener;

		// Setup the GUI
		setLayout(new MigLayout("", "[]", ""));

		srcColorL = new JLabel("Spacecraft:", JLabel.RIGHT);
		srcColorB = GuiUtil.formButton(this, "");
		add(srcColorL, "sg g1");
		add(srcColorB, "sg g2,wrap");

		tgtColorL = new JLabel("Target:", JLabel.RIGHT);
		tgtColorB = GuiUtil.formButton(this, "");
		add(tgtColorL, "sg g1");
		add(tgtColorB, "sg g2");

		srcColor = aSrcColor;
		tgtColor = aTgtColor;

		updateGui();
	}

	/**
	 * Returns the {@link GroupColorProvider} that should be used to color lidar
	 * data points associated with the source (spacecraft).
	 */
	public GroupColorProvider getGroupColorProviderSource()
	{
		return new ConstGroupColorProvider(new SimpleColorProvider(srcColor));
	}

	/**
	 * Returns the {@link GroupColorProvider} that should be used to color lidar
	 * data points associated with the target.
	 */
	public GroupColorProvider getGroupColorProviderTarget()
	{
		return new ConstGroupColorProvider(new SimpleColorProvider(tgtColor));
	}

	/**
	 * Sets in the installed colors.
	 */
	public void setInstalledColors(Color aSrcColor, Color aTgtColor)
	{
		srcColor = aSrcColor;
		tgtColor = aTgtColor;

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		// Process the event
		Object source = aEvent.getSource();
		if (source == srcColorB)
			doUpdateSourceColor();
		else if (source == tgtColorB)
			doUpdateTargetColor();

		// Notify our refListener
		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

	@Override
	public void activate(boolean aIsActive)
	{
		updateGui();
	}

	@Override
	public GroupColorProvider getGroupColorProvider()
	{
		// Logic error: A more specific method should have been called.
		throw new RuntimeException(
				"Please call a more specific method: getGroupColorProviderSource() or getGroupColorProviderTarget()");
	}

	/**
	 * Helper method that handles the action for srcColorB
	 */
	private void doUpdateSourceColor()
	{
		// Prompt the user for a color
		Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", srcColor);
		if (tmpColor == null)
			return;
		srcColor = tmpColor;

		updateGui();
	}

	/**
	 * Helper method that handles the action for tgtColorB
	 */
	private void doUpdateTargetColor()
	{
		// Prompt the user for a color
		Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", tgtColor);
		if (tmpColor == null)
			return;
		tgtColor = tmpColor;

		updateGui();
	}

	/**
	 * Helper method that will update the UI to reflect the user selected colors.
	 */
	private void updateGui()
	{
		int iconW = (int) (srcColorL.getWidth() * 0.60);
		int iconH = (int) (srcColorL.getHeight() * 0.80);

		// Update the source / target icons
		Icon srcIcon = new ColorIcon(srcColor, Color.BLACK, iconW, iconH);
		srcColorB.setIcon(srcIcon);

		Icon tgtIcon = new ColorIcon(tgtColor, Color.BLACK, iconW, iconH);
		tgtColorB.setIcon(tgtIcon);
	}

}
