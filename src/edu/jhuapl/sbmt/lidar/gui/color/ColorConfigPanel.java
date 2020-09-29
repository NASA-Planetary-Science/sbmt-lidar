package edu.jhuapl.sbmt.lidar.gui.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.gui.AutoColorPanel;
import edu.jhuapl.saavtk.color.gui.EditGroupColorPanel;
import edu.jhuapl.saavtk.color.gui.RandomizePanel;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.lidar.LidarManager;

import glum.gui.GuiExeUtil;
import glum.gui.component.GComboBox;
import glum.gui.panel.CardPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to allow a user to configure the LidarColorProvider used for
 * coloring lidar data.
 *
 * @author lopeznr1
 */
public class ColorConfigPanel<G1> extends JPanel implements ActionListener
{
	// Ref vars
	private final ActionListener refListener;

	// GUI vars
	private LidarColorBarPanel<G1> colorMapPanel;
	private CardPanel<EditGroupColorPanel> colorPanel;
	private GComboBox<ColorMode> colorModeBox;

	/**
	 * Standard Constructor
	 */
	public ColorConfigPanel(ActionListener aListener, LidarManager<G1> aManager, Renderer aRenderer)
	{
		refListener = aListener;

		setLayout(new MigLayout("", "0[][]0", "0[][]0"));

		JLabel tmpL = new JLabel("Colorize:");
		colorModeBox = new GComboBox<>(this, ColorMode.values());
		add(tmpL);
		add(colorModeBox, "growx,wrap 2");

		colorMapPanel = new LidarColorBarPanel<>(this, aManager, aRenderer);
		colorPanel = new CardPanel<>();
		colorPanel.addCard(ColorMode.AutoHue, new AutoColorPanel(this));
		colorPanel.addCard(ColorMode.ColorMap, colorMapPanel);
		colorPanel.addCard(ColorMode.Randomize, new RandomizePanel(this, 0));
		colorPanel.addCard(ColorMode.Simple, new SimplePanel(this, Color.GREEN, Color.BLUE));

		add(colorPanel, "growx,growy,span");

		// Custom initialization code
		Runnable tmpRunnable = () -> {
			colorPanel.getActiveCard().activate(true);
		};
		GuiExeUtil.executeOnceWhenShowing(this, tmpRunnable);
	}

	/**
	 * Returns the GroupColorProvider that should be used to color data points
	 * associated with the lidar source (spacecraft).
	 */
	public GroupColorProvider getSourceGroupColorProvider()
	{
		EditGroupColorPanel tmpPanel = colorPanel.getActiveCard();
		if (tmpPanel instanceof SimplePanel)
			return ((SimplePanel) tmpPanel).getGroupColorProviderSource();

		return tmpPanel.getGroupColorProvider();
	}

	/**
	 * Returns the GroupColorProvider that should be used to color data points
	 * associated with the lidar target (ground).
	 */
	public GroupColorProvider getTargetGroupColorProvider()
	{
		EditGroupColorPanel tmpPanel = colorPanel.getActiveCard();
		if (tmpPanel instanceof SimplePanel)
			return ((SimplePanel) tmpPanel).getGroupColorProviderTarget();

		return tmpPanel.getGroupColorProvider();
	}

	/**
	 * Sets the ColorProviderMode which will be active
	 */
	public void setActiveMode(ColorMode aMode)
	{
		colorPanel.getActiveCard().activate(false);

		colorModeBox.setChosenItem(aMode);
		colorPanel.switchToCard(aMode);
		colorPanel.getActiveCard().activate(true);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == colorModeBox)
			doUpdateColorPanel();

		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

	/**
	 * Helper method to properly update the colorPanel.
	 */
	private void doUpdateColorPanel()
	{
		colorPanel.getActiveCard().activate(false);

		ColorMode tmpCM = colorModeBox.getChosenItem();
		colorPanel.switchToCard(tmpCM);
		colorPanel.getActiveCard().activate(true);
	}

}
