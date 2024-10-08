package edu.jhuapl.sbmt.lidar.vtk;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.LodUtil;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.saavtk.vtk.VtkResource;
import edu.jhuapl.sbmt.lidar.LidarManager;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.util.LidarGeoUtil;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkUnsignedCharArray;

/**
 * Class which contains the logic to render a single selected lidar point using
 * the VTK framework.
 * <P>
 * This class supports the following configurable state:
 * <UL>
 * <LI>Selected item and corresponding LidarPoint.
 * <LI>Point color
 * <LI>Point size
 * </UL>
 *
 * @author lopeznr1
 */
public class VtkPointPainter<G1> implements ItemEventListener, VtkResource
{
	// Reference vars
	private final LidarManager<G1> refManager;

	// State vars
	private G1 workItem;
	private LidarPoint workPoint;
	private Color pointColor;
	private boolean isStale;

	// VTK vars
	private VtkLodActor vActor;
	private vtkPolyData vPolydata;
	private vtkPolyDataMapper vPointsRegPDM;

	/**
	 * Standard Constructor
	 *
	 * @param aManager
	 */
	public VtkPointPainter(LidarManager<G1> aManager)
	{
		refManager = aManager;

		workItem = null;
		workPoint = null;
		pointColor = new Color(0.1f, 0.1f, 1.0f);
		isStale = false;

		vPolydata = new vtkPolyData();
		VtkUtil.clearPolyData(vPolydata);

		vPointsRegPDM = new vtkPolyDataMapper();
		vPointsRegPDM.SetInputData(vPolydata);

		vActor = new VtkLodActor(null);
		vActor.setDefaultMapper(vPointsRegPDM);
		vActor.setLodMapper(LodMode.MaxQuality, vPointsRegPDM);

		vtkPolyDataMapper vPointsDecPDM = LodUtil.createQuadricDecimatedMapper(vPolydata);
		vActor.setLodMapper(LodMode.MaxSpeed, vPointsDecPDM);
		vActor.GetProperty().SetColor(pointColor.getRed() / 255.0, pointColor.getGreen() / 255.0,
				pointColor.getBlue() / 255.0);
		vActor.GetProperty().SetPointSize(1.0f);

		// Register for events of interst
		refManager.addListener(this);
	}

	/**
	 * Returns the vtkActor associated with the VtkPointPainter.
	 */
	public vtkActor getActor()
	{
		return vActor;
	}

	/**
	 * Returns the item associated with this VtkPainter.
	 */
	public G1 getItem()
	{
		return workItem;
	}

	/**
	 * Returns the LidarPoint associated with this VtkPainter.
	 */
	public LidarPoint getPoint()
	{
		return workPoint;
	}

	/**
	 * Sets in the working item and corresponding point.
	 */
	public void setData(G1 aItem, LidarPoint aPoint)
	{
		workItem = aItem;
		workPoint = aPoint;

		isStale = true;
	}

	/**
	 * Sets in the point size associated with this VtkLidarPoint.
	 */
	public void setPointSize(double aSize)
	{
		vActor.GetProperty().SetPointSize((float)aSize);
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsChanged)
		{
			setData(null, null);
			isStale = true;
		}
		if (aEventType == ItemEventType.ItemsMutated)
		{
			isStale = true;
		}
	}

	@Override
	public void vtkDispose()
	{
		vActor.Delete();
		vPolydata.Delete();
		vPointsRegPDM.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		// Bail if we are not stale
		if (isStale == false)
			return;

		VtkUtil.clearPolyData(vPolydata);
		vtkPoints points = vPolydata.GetPoints();
		vtkCellArray vert = vPolydata.GetVerts();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray) vPolydata.GetCellData().GetScalars();

		if (workPoint != null && workItem != null && refManager.getIsVisible(workItem) == true)
		{
			double radialOffset = refManager.getRadialOffset();
			Vector3D translationV = refManager.getTranslation(workItem);
			Vector3D targetV = workPoint.getTargetPosition();
			targetV = LidarGeoUtil.transformTarget(translationV, radialOffset, targetV);

			int id1 = (int)points.InsertNextPoint(targetV.getX(), targetV.getY(), targetV.getZ());
			vtkIdList idList = new vtkIdList();
			idList.InsertNextId(id1);

//			vtkPoints points = new vtkPoints();
//			points.InsertNextPoint(targPosArr);
//			polydata.SetPoints(points);

			vert.InsertNextCell(idList);
			colors.InsertNextTuple4(pointColor.getRed(), pointColor.getGreen(), pointColor.getBlue(), 255);
		}
//
//		vPolydata.Modified();

		isStale = false;
	}

}
