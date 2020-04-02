package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.EditMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtilEx;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Class which provides the functionality that enables a user to translate a
 * lidar track to a new location. All points associated with the lidar track
 * will be translated an equal amount.
 * <P>
 * The translation action is triggered when the user selects on a specific point
 * on the lidar track and then proceeds to drag that point to a new location.
 */
public class LidarShiftPicker extends Picker implements ItemEventListener
{
	// Reference vars
	private ModelManager refModelManager;
	private LidarTrackManager refTrackManager;
	private PolyhedralModel refSmallBody;
	private vtkJoglPanelComponent refRenWin;

	// VTK vars
	private vtkCellPicker smallBodyPicker;
	private vtkCellPicker pointPicker;

	// State vars
	private EditMode currEditMode;
	private Vector3D origPt;

	public LidarShiftPicker(LidarTrackManager aTrackManager, Renderer aRenderer, ModelManager aModelManager)
	{
		refModelManager = aModelManager;
		refTrackManager = aTrackManager;
		refSmallBody = aModelManager.getPolyhedralModel();
		refRenWin = aRenderer.getRenderWindowPanel();

		smallBodyPicker = PickUtilEx.formSmallBodyPicker(refSmallBody);
		pointPicker = PickUtilEx.formEmptyPicker();

		currEditMode = EditMode.CLICKABLE;
		origPt = null;

		// Register for events of interest
		refTrackManager.addListener(this);
	}

	@Override
	public int getCursorType()
	{
		if (currEditMode == EditMode.DRAGGABLE)
			return Cursor.HAND_CURSOR;

		return Cursor.CROSSHAIR_CURSOR;
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		// Bail if the list of items has not changed
		if (aEventType != ItemEventType.ItemsChanged)
			return;

		PickUtilEx.updatePickerProps(pointPicker, refTrackManager.getProps());
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (origPt != null)
			return true;

		return false;
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// Assume nothing will be picked
		origPt = null;

		// Bail if we are not ready to do a drag operation
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if the button-1 or button-3 are not selected
		if (aEvent.getButton() != MouseEvent.BUTTON1 && aEvent.getButton() != MouseEvent.BUTTON3)
			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		// Bail if we fail to pick something via our pointPicker
		pickSucceeded = doPick(aEvent, pointPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		// Retrieve the position of the selected LidarPoint
		vtkActor tmpActor = pointPicker.GetActor();
		int tmpCellId = pointPicker.GetCellId();
		PickTarget tmpPickTarg = new PickTarget(tmpActor, Vector3D.ZERO, Vector3D.ZERO, tmpCellId);
		LidarPoint tmpLP = refTrackManager.getLidarPointFrom(tmpPickTarg);
		if (tmpLP == null)
			return;

		origPt = tmpLP.getTargetPosition();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// Reset the state vars
		origPt = null;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not in the proper edit mode
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if do not have a valid origPt
		if (origPt == null)
			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model != refSmallBody)
			return;

		// Perform the translation
		double[] currPt = smallBodyPicker.GetPickPosition();
		Set<LidarTrack> tmpS = refTrackManager.getSelectedItems();
		Vector3D tmpVect = new Vector3D(currPt[0] - origPt.getX(), currPt[1] - origPt.getY(), currPt[2] - origPt.getZ());
		refTrackManager.setTranslation(tmpS, tmpVect);
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		int pickSucceeded = doPick(aEvent, pointPicker, refRenWin);
		if (pickSucceeded == 1)
			currEditMode = EditMode.DRAGGABLE;
		else
			currEditMode = EditMode.CLICKABLE;

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

}
