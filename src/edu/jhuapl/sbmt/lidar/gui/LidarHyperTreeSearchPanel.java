package edu.jhuapl.sbmt.lidar.gui;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import com.google.common.base.Stopwatch;

import vtk.vtkCubeSource;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.LidarDataSource;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PointInRegionChecker;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.NonexistentRemoteFile;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.lidar.LidarSearchParms;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;
import edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2.Hayabusa2LidarHypertreeSkeleton;
import edu.jhuapl.sbmt.lidar.util.LidarQueryUtil;
import edu.jhuapl.sbmt.lidar.util.LidarQueryUtil.DataType;

public class LidarHyperTreeSearchPanel extends LidarSearchPanel
{
	// Reference vars
	private final DataType refDataType;

	// State vars
	private Map<LidarDataSource, FSHyperTreeSkeleton> skeletonM;

	public LidarHyperTreeSearchPanel(BodyViewConfig aBodyViewConfig, ModelManager aModelManager,
			PickManager aPickManager, LidarTrackManager aTrackManager, DataType aDataType)
	{
		super(aBodyViewConfig, aModelManager, aPickManager, aTrackManager);

		refDataType = aDataType;

		// Currently Hayabusa supports spacecraft range constraints
		if (refDataType == DataType.Hayabusa)
			addSpacecraftRangeArea();

		skeletonM = new HashMap<>();
	}

	@Override
	protected void executeQuery(LidarTrackManager aManager, LidarSearchParms aSearchParms,
			PointInRegionChecker aPointInRegionChecker, double aTimeSeparationBetweenTracks, int aMinTrackLength)
			throws IOException
	{
		System.out.println("LidarHyperTreeSearchPanel: executeQuery: executing query");
		FSHyperTreeSkeleton tmpSkeleton = skeletonM.get(aSearchParms.getDataSource());

		// Delegate
		LidarQueryUtil.executeQueryHyperTree(aManager, aSearchParms, this, refDataType, tmpSkeleton,
				aPointInRegionChecker);
	}

	@Override
	protected void handleActionSubmit(LidarDataSource aDataSource, AbstractEllipsePolygonModel aSelectionRegion)
	{
		System.out.println("LidarHyperTreeSearchPanel: handleActionSubmit: ");
		// Retrieve the appropriate skeleton
		FSHyperTreeSkeleton tmpSkeleton = skeletonM.get(aDataSource);

		try
		{
			// Load the skeleton, if it has not been loaded yet
			if (tmpSkeleton == null)
			{
				tmpSkeleton = formSkelton(aDataSource);
				tmpSkeleton.read();

				skeletonM.put(aDataSource, tmpSkeleton);
			}
		}
		catch (NonexistentRemoteFile aExp)
		{
			JOptionPane.showMessageDialog(this, "There is no existing tree for this phase", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (UnauthorizedAccessException aExp)
		{
			JOptionPane.showMessageDialog(this, "You do not have access to these data as the logged in user.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		vtkPolyData interiorPoly = new vtkPolyData();
		if (aSelectionRegion.getNumItems() > 0)
		{
         int numberOfSides = aSelectionRegion.getNumberOfSides();
			Ellipse region = aSelectionRegion.getItem(0);

			// Always use the lowest resolution model for getting the intersection
			// cubes list. Therefore, if the selection region was created using a
			// higher resolution model, we need to recompute the selection region
			// using the low res model.
			if (refSmallBodyModel.getModelResolution() > 0)
				refSmallBodyModel.drawRegularPolygonLowRes(region.getCenter().toArray(), region.getRadius(),
						numberOfSides, interiorPoly, null); // this sets
																						// interiorPoly
			else
				interiorPoly = aSelectionRegion.getVtkInteriorPolyDataFor(region);
		}
		else
		{
			vtkCubeSource box = new vtkCubeSource();
			double[] bboxBounds = refSmallBodyModel.getBoundingBox().getBounds();
			BoundingBox bbox = new BoundingBox(bboxBounds);
			bbox.increaseSize(0.01);
			box.SetBounds(bbox.getBounds());
			box.Update();
			interiorPoly.DeepCopy(box.GetOutput());
		}

		double[] bounds = interiorPoly.GetBounds();
		double[] rangeLims = getSpaceCraftRangeConstraint();
		double[] timeLims = getTimeConstraint();

		Stopwatch sw = Stopwatch.createStarted();
		TreeSet<Integer> cubeList = getLeavesIntersectingBoundingBox(tmpSkeleton, new BoundingBox(bounds), timeLims,
				rangeLims);
//		System.out.println(cubeList);
//		System.out.println("Search Time="+sw.elapsedMillis()+" ms");
		sw.stop();
		System.out.println("LidarHyperTreeSearchPanel: handleActionSubmit: showing data");
		showData(cubeList, aSelectionRegion);
	}

	/**
	 * Creates a skeleton for the specified DataSource.
	 */
	private FSHyperTreeSkeleton formSkelton(LidarDataSource aDataSource)
	{
		Path basePath = Paths.get(aDataSource.getPath());

		// Create the appropriate skelton and add it in
		FSHyperTreeSkeleton retSkeleton;
		if (refDataType == DataType.Mola)
			retSkeleton = new FSHyperTreeSkeleton(basePath);
		else if (refDataType == DataType.Ola)
			retSkeleton = new FSHyperTreeSkeleton(basePath);
		else if (refDataType == DataType.Hayabusa)
			retSkeleton = new Hayabusa2LidarHypertreeSkeleton(basePath);
		else
			throw new RuntimeException("Unrecognized DataType: " + refDataType);

		return retSkeleton;
	}

	/**
	 * Utility helper method to retrieve the proper set of cubes
	 */
	private static TreeSet<Integer> getLeavesIntersectingBoundingBox(FSHyperTreeSkeleton aSkeleton, BoundingBox bbox,
			double[] tlims, double[] scrangeLims)
	{
		double[] bounds;
		if (scrangeLims == null)
			bounds = new double[] { bbox.xmin, bbox.xmax, bbox.ymin, bbox.ymax, bbox.zmin, bbox.zmax, tlims[0], tlims[1] };
		else
			bounds = new double[] { bbox.xmin, bbox.xmax, bbox.ymin, bbox.ymax, bbox.zmin, bbox.zmax, tlims[0], tlims[1],
					scrangeLims[0], scrangeLims[1] };
		return aSkeleton.getLeavesIntersectingBoundingBox(bounds);
	}

}
