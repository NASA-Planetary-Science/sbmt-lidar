package edu.jhuapl.sbmt.lidar.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.RangeSlider;

/**
 * Custom slider used to specify a low and high value.
 */
public class PercentIntervalChanger extends JPanel implements ChangeListener
{
	// State vars
	private List<ActionListener> listenerL;

	// GUI vars
	private RangeSlider slider;

	public PercentIntervalChanger(String aTitle)
	{
		listenerL = new ArrayList<>();

//		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		setBorder(BorderFactory.createTitledBorder("Displayed Lidar Data"));

		slider = new RangeSlider(0, 255, 0, 255);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(10);
		slider.setPaintTrack(true);
		slider.addChangeListener(this);
		add(slider);
	}

	/**
	 * Registers an ActionListener with this component.
	 */
	public void addActionListener(ActionListener aListener)
	{
		listenerL.add(aListener);
	}

	/**
	 * Deregisters an ActionListener with this component.
	 */
	public void delActionListener(ActionListener aListener)
	{
		listenerL.remove(aListener);
	}

	/**
	 * Returns the low value.
	 */
	public double getLowValue()
	{
		double maxVal = slider.getMaximum();
		double retVal = slider.getLowValue() / maxVal;
		return retVal;
	}

	/**
	 * Returns the high value.
	 */
	public double getHighValue()
	{
		double maxVal = slider.getMaximum();
		double retVal = slider.getHighValue() / maxVal;
		return retVal;
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		for (ActionListener aListener : listenerL)
			aListener.actionPerformed(new ActionEvent(this, 0, "update"));
	}
}
