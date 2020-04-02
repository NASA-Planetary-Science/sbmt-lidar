package edu.jhuapl.sbmt.lidar.gui.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.gui.LidarPlot;
import edu.jhuapl.sbmt.lidar.util.LidarGeoUtil;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Plot Track".
 *
 * @author lopeznr1
 */
class PlotTrackAction extends PopAction<LidarTrack>
{
	// Ref vars
	private final LidarTrackManager refManager;

	/**
	 * Standard Constructor
	 */
	public PlotTrackAction(LidarTrackManager aManager)
	{
		refManager = aManager;
	}

	@Override
	public void executeAction(List<LidarTrack> aItemL)
	{
		LidarTrack tmpTrack = aItemL.get(0);

		try
		{
			List<Double> potentialL = new ArrayList<>();
			List<Double> accelerationL = new ArrayList<>();
			List<Double> elevationL = new ArrayList<>();
			List<Double> distanceL = new ArrayList<>();
			List<Double> timeL = new ArrayList<>();

			LidarGeoUtil.getGravityDataForTrack(refManager, tmpTrack, potentialL, accelerationL, elevationL, distanceL,
					timeL);

			LidarPlot lidarPlot = new LidarPlot(refManager, tmpTrack, potentialL, distanceL, timeL, "Potential", "J/kg");
			lidarPlot.setVisible(true);
			lidarPlot = new LidarPlot(refManager, tmpTrack, accelerationL, distanceL, timeL, "Acceleration", "m/s^2");
			lidarPlot.setVisible(true);
			lidarPlot = new LidarPlot(refManager, tmpTrack, elevationL, distanceL, timeL, "Elevation", "m");
			lidarPlot.setVisible(true);
		}
		catch (Exception aExp)
		{
			aExp.printStackTrace();
		}
	}

	@Override
	public void setChosenItems(Collection<LidarTrack> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the plot menuitem if the number of selected items == 1
		boolean isEnabled = aItemC.size() == 1;
		aAssocMI.setEnabled(isEnabled);
	}

}