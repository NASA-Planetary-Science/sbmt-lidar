package edu.jhuapl.sbmt.lidar.gui.color;

import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * {@link LidarColorConfigPanel} that provides the AutoHue color panel. There
 * are no configuration options.
 *
 * @author lopeznr1
 */
public class AutoColorPanel extends JPanel implements LidarColorConfigPanel
{
	/**
	 * Standard Constructor
	 */
	public AutoColorPanel(ActionListener aListener)
	{
		// Setup the GUI
		setLayout(new MigLayout("", "0[]0", ""));

		JLabel tmpL = new JLabel("There are no configuration options.", JLabel.CENTER);
		add(tmpL, "growx,pushx");
	}

	@Override
	public void activate(boolean aIsActive)
	{
		; // Nothing to do
	}

	@Override
	public GroupColorProvider getSourceGroupColorProvider()
	{
		return ColorWheelGroupColorProvider.Instance;
	}

	@Override
	public GroupColorProvider getTargetGroupColorProvider()
	{
		return ColorWheelGroupColorProvider.Instance;
	}

}
