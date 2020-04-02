package edu.jhuapl.sbmt.lidar.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.gui.LidarSaveDialog;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Save Track".
 *
 * @author lopeznr1
 */
class SaveTrackAction extends PopAction<LidarTrack>
{
	// Ref vars
	private final LidarTrackManager refManager;
	private final Component refParent;

	// State vars
	private LidarSaveDialog saveDialog;

	/**
	 * Standard Constructor
	 */
	public SaveTrackAction(LidarTrackManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<LidarTrack> aItemL)
	{
		if (saveDialog == null)
			saveDialog = new LidarSaveDialog(refParent, refManager);

		saveDialog.setVisible(true);
	}

	@Override
	public void setChosenItems(Collection<LidarTrack> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Update our associated MenuItem
		String displayStr = MessageUtil.toPluralForm("Save Track", aItemC);
		aAssocMI.setText(displayStr);
	}
}