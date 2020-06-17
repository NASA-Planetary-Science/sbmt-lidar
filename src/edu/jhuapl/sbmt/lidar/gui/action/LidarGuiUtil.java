package edu.jhuapl.sbmt.lidar.gui.action;

import java.awt.Component;

import javax.swing.JMenu;

import vtk.vtkProp;

import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.view.AssocActor;
import edu.jhuapl.sbmt.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.lidar.LidarManager;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.vtk.VtkLidarPainter;

import glum.gui.action.PopupMenu;

/**
 * Collection of lidar UI utility methods.
 *
 * @author lopeznr1
 */
public class LidarGuiUtil
{
	/**
	 * Forms the popup menu associated with lidar files.
	 */
	public static PopupMenu<LidarFileSpec> formLidarFileSpecPopupMenu(LidarFileSpecManager aManager, Component aParent)
	{
		PopupMenu<LidarFileSpec> retPM = new PopupMenu<>(aManager);

		JMenu colorMenu = new JMenu("File Color");
		retPM.installPopAction(new MultiColorLidarAction<>(aManager, aParent, colorMenu), colorMenu);

		retPM.installPopAction(new SaveFileAction(aManager, aParent), "Save File");
		retPM.installPopAction(new HideShowLidarAction<>(aManager, "File"), "Show File");
		retPM.installPopAction(new HideOtherLidarAction<>(aManager), "Hide Other Files");

		return retPM;
	}

	/**
	 * Forms the popup menu associated with lidar tracks.
	 */
	public static PopupMenu<LidarTrack> formLidarTrackPopupMenu(LidarTrackManager aManager, Component aParent)
	{
		PopupMenu<LidarTrack> retPM = new PopupMenu<>(aManager);

		JMenu colorMenu = new JMenu("Track Color");
		retPM.installPopAction(new MultiColorLidarAction<>(aManager, aParent, colorMenu), colorMenu);

		retPM.installPopAction(new SaveTrackAction(aManager, aParent), "Save Track");
		retPM.installPopAction(new HideShowLidarAction<>(aManager, "Track"), "Show Track");
		retPM.installPopAction(new HideOtherLidarAction<>(aManager), "Hide Other Tracks");
		retPM.installPopAction(new TranslateTrackAction(aManager, aParent), "Translate Track");
		if (Configuration.isAPLVersion() == true)
			retPM.installPopAction(new PlotTrackAction(aManager), "Plot Track...");

		return retPM;
	}

	/**
	 * Utility method that returns true if the specified {@link PickTarget} is
	 * associated with the provided {@link StructureManager}.
	 */
	public static boolean isAssociatedPickTarget(PickTarget aPickTarget, LidarManager<?> aManager)
	{
		// Bail if the actor is not the right type
		vtkProp tmpProp = aPickTarget.getActor();
		if (tmpProp instanceof AssocActor == false)
			return false;

		// Bail if tmpProp is not associated with the LidarManager
		VtkLidarPainter<?> tmpPainter = ((AssocActor) tmpProp).getAssocModel(VtkLidarPainter.class);
		if (tmpPainter == null || tmpPainter.getManager() != aManager)
			return false;

		return true;
	}

}
