package edu.jhuapl.sbmt.lidar.vtk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.feature.ConstFeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.LodUtil;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.sbmt.lidar.BasicLidarPoint;
import edu.jhuapl.sbmt.lidar.LidarFeatureType;
import edu.jhuapl.sbmt.lidar.LidarManager;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.util.LidarGeoUtil;
import vtk.vtkCellArray;
import vtk.vtkGeometryFilter;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

/**
 * Class used to render a single lidar data object and the corresponding points
 * via the VTK framework.
 * <p>
 * This class supports the following configurable state:
 * <ul>
 * <li>Source / Target ColorProviders
 * <li>Translation vector
 * <li>Point size
 * <li>Range of start / stop percent (TODO: Not intuitive)
 * </ul>
 *
 * @author lopeznr1
 */
public class VtkLidarUniPainter<G1> implements VtkLidarPainter<G1>
{
	// Reference vars
	private final LidarManager<G1> refManager;
	private final G1 refItem;

	// Attributes
	private final FeatureAttr timeFA;
	private final FeatureAttr radiusFA;
	private final FeatureAttr rangeFA;
	private final FeatureAttr intensityFA;

	// Cache vars
	private ColorProvider cSourceCP;
	private ColorProvider cTargetCP;
	private Vector3D cTranslationV;
	private double cRadialOffset;

	// State vars
	private double percentBeg;
	private double percentEnd;
	private boolean isHighlightSelection;

	// VTK vars
	private vtkPolyData vSourcePD;
	private vtkPolyData vTargetPD;
	private vtkGeometryFilter vSourceGF;
	private vtkGeometryFilter vTargetGF;
	private vtkPoints vSourceP;
	private vtkPoints vTargetP;
	private vtkCellArray vSourceCA;
	private vtkCellArray vTargetCA;
	private vtkUnsignedCharArray vColorUCA;
	private VtkLodActor vSourceA;
	private VtkLodActor vTargetA;

	/**
	 * Standard Constructor
	 *
	 * @param aManager The {@link LidarManager} responsible for the specified
	 * lidar item.
	 * @param aItem The lidar item of interest.
	 * @param aVLS The {@link VtkLidarStruct} corresponding to the specified
	 * lidar item.
	 * <P>
	 * The contents of the {@link VtkLidarStruct} will be managed by this
	 * painter. Do not keep or pass on references of the contents of the
	 * {@link VtkLidarStruct}.
	 */
	public VtkLidarUniPainter(LidarManager<G1> aManager, G1 aItem, VtkLidarStruct aVLS)
	{
		refManager = aManager;
		refItem = aItem;

		timeFA = aVLS.timeFA;
		radiusFA = aVLS.radiusFA;
		rangeFA = aVLS.rangeFA;
		intensityFA = aVLS.intensityFA;

		cSourceCP = null;
		cTargetCP = null;
		cTranslationV = Vector3D.ZERO;
		cRadialOffset = 0.0;

		percentBeg = 0.0;
		percentEnd = 1.0;
		isHighlightSelection = false;

		doInitialVtkSetup(aVLS.vSrcP, aVLS.vSrcCA, aVLS.vTgtP, aVLS.vTgtCA);
	}

	@Override
	public FeatureAttr getFeatureAttrFor(FeatureType aFeatureType)
	{
		if (aFeatureType == LidarFeatureType.Time)
			return timeFA;
		else if (aFeatureType == LidarFeatureType.Radius)
			return radiusFA;
		else if (aFeatureType == LidarFeatureType.Range)
			return rangeFA;
		else if (aFeatureType == LidarFeatureType.Intensity)
			return intensityFA;
		else if (aFeatureType == null)
			return new ConstFeatureAttr(getNumberOfPoints(), 0.0, 1.0, 0.5);

		throw new RuntimeException("FeatureType is not supported: " + aFeatureType);
	}

	@Override
	public G1 getLidarItemForCell(int aCellId)
	{
		return refItem;
	}

	@Override
	public LidarPoint getLidarPointForCell(int aCellId)
	{
		var cellId = vTargetGF.GetPointMinimum() + aCellId;
		var srcPtArr = vSourceP.GetPoint(cellId);
		var tgtPtArr = vTargetP.GetPoint(cellId);
		var timeVal = timeFA.getValAt((int)cellId);
		var rangeVal = rangeFA.getValAt((int)cellId);
		var intensityVal = intensityFA.getValAt((int)cellId);
		return new BasicLidarPoint(tgtPtArr, srcPtArr, timeVal, rangeVal, intensityVal);
	}

	@Override
	public LidarManager<G1> getManager()
	{
		return refManager;
	}

	@Override
	public List<vtkProp> getProps()
	{
		ArrayList<vtkProp> retL = new ArrayList<>();
		retL.add(vTargetA);
		if (vSourceA.GetVisibility() != 0)
			retL.add(vSourceA);

		return retL;
	}

	@Override
	public void setHighlightSelection(boolean aBool)
	{
		isHighlightSelection = aBool;
	}

	@Override
	public void setPercentageShown(double aPercentBeg, double aPercentEnd)
	{
		percentBeg = aPercentBeg;
		percentEnd = aPercentEnd;

		int numberOfPoints = getNumberOfPoints();
		int firstPointId = (int) (numberOfPoints * percentBeg);
		int lastPointId = (int) (numberOfPoints * percentEnd) - 1;
		if (lastPointId < firstPointId)
			lastPointId = firstPointId;

		vTargetGF.SetPointMinimum(firstPointId);
		vTargetGF.SetPointMaximum(lastPointId);
		vTargetGF.Update();

		vSourceGF.SetPointMinimum(firstPointId);
		vSourceGF.SetPointMaximum(lastPointId);
		vSourceGF.Update();
	}

	@Override
	public void setPointSize(double aPointSize)
	{
		double tmpPointSize = aPointSize;
		if (isHighlightSelection == true && refManager.getSelectedItems().contains(refItem) == true)
			tmpPointSize = tmpPointSize * 2;

		// Bail if no change to point size
		if (vSourceA.GetProperty().GetPointSize() == tmpPointSize)
			return;

		vSourceA.GetProperty().SetPointSize((float)tmpPointSize);
		vTargetA.GetProperty().SetPointSize((float)tmpPointSize);

		vSourceA.Modified();
		vTargetA.Modified();
	}

	@Override
	public void setShowSourcePoints(boolean aBool)
	{
		if (vSourceA != null)
			vSourceA.SetVisibility(aBool ? 1 : 0);
	}

	@Override
	public void vtkDispose()
	{
		timeFA.dispose();
		radiusFA.dispose();
		rangeFA.dispose();
		intensityFA.dispose();

		vSourcePD.Delete();
		vTargetPD.Delete();
		vSourceGF.Delete();
		vTargetGF.Delete();
		vSourceP.Delete();
		vTargetP.Delete();
		vSourceCA.Delete();
		vTargetCA.Delete();
		vColorUCA.Delete();

		vSourceA.Delete();
		vTargetA.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		// Update color state
		ColorProvider tmpSourceCP = refManager.getColorProviderSource(refItem);
		ColorProvider tmpTargetCP = refManager.getColorProviderTarget(refItem);

		boolean isStaleColor = false;
		isStaleColor |= Objects.equals(cSourceCP, tmpSourceCP) == false;
		isStaleColor |= Objects.equals(cTargetCP, tmpTargetCP) == false;
		if (isStaleColor == true)
			doUpdateStateColor(tmpSourceCP, tmpTargetCP);

		// Update translation state
		Vector3D tmpTranslationV = refManager.getTranslation(refItem);
		double tmpRadialOffset = refManager.getRadialOffset();

		boolean isStaleTranslation = Objects.equals(cTranslationV, tmpTranslationV) == false;
		isStaleTranslation |= cRadialOffset != tmpRadialOffset;
		if (isStaleTranslation == true)
			doUpdateStateTranslation(tmpTranslationV, tmpRadialOffset);
	}

	/**
	 * Helper method that performs the initial VTK setup of source/target points
	 * and colors.
	 */
	private void doInitialVtkSetup(vtkPoints aSrcP, vtkCellArray aSrcCA, vtkPoints aTgtP, vtkCellArray aTgtCA)
	{
		// VTK vars
		vSourceP = aSrcP;
		vSourceCA = aSrcCA;
		vSourcePD = new vtkPolyData();
		vSourcePD.SetPoints(vSourceP);
		vSourcePD.SetVerts(vSourceCA);

		vTargetP = aTgtP;
		vTargetCA = aTgtCA;
		vTargetPD = new vtkPolyData();
		vTargetPD.SetPoints(vTargetP);
		vTargetPD.SetVerts(vTargetCA);
		vColorUCA = new vtkUnsignedCharArray();
		vColorUCA.SetNumberOfComponents(4);
		vTargetPD.GetCellData().SetScalars(vColorUCA);

		// Cache vars
		cSourceCP = refManager.getColorProviderSource(refItem);
		cTargetCP = refManager.getColorProviderTarget(refItem);

		// Instantiate VTK vars (associated with the source points)
		int numPts = getNumberOfPoints();
		vSourceGF = VtkUtil.formGeometryFilter(vSourcePD, numPts);

		vtkPolyDataMapper vSourceRegPDM = new vtkPolyDataMapper();
		vSourceRegPDM.SetInputConnection(vSourceGF.GetOutputPort());

		vSourceA = new VtkLodActor(this);
		vSourceA.setDefaultMapper(vSourceRegPDM);
		vSourceA.setLodMapper(LodMode.MaxQuality, vSourceRegPDM);

		vtkPolyDataMapper vSourceDecPDM = LodUtil.createQuadricDecimatedMapper(vSourceGF.GetOutputPort());
		vSourceA.setLodMapper(LodMode.MaxSpeed, vSourceDecPDM);

		Color tmpColorA = cSourceCP.getColor(0.0, 1.0, 0.5);
		double r = tmpColorA.getRed() / 255.0;
		double g = tmpColorA.getGreen() / 255.0;
		double b = tmpColorA.getBlue() / 255.0;
		vSourceA.GetProperty().SetColor(r, g, b);
		vSourceA.GetProperty().SetPointSize(2.0f);

		// Instantiate VTK vars (associated with the target points)
		FeatureType tmpFT = cTargetCP.getFeatureType();
		FeatureAttr tmpFA = getFeatureAttrFor(tmpFT);

		double minVal = tmpFA.getMinVal();
		double maxVal = tmpFA.getMaxVal();
		for (int aIdx = 0; aIdx < numPts; aIdx++)
		{
			// Delegate to the ColorProvider for the actual color value
			double tmpVal = tmpFA.getValAt(aIdx);
			Color tmpColorB = cTargetCP.getColor(minVal, maxVal, tmpVal);
			vColorUCA.InsertNextTuple4(tmpColorB.getRed(), tmpColorB.getGreen(), tmpColorB.getBlue(),
					tmpColorB.getAlpha());
		}

		vTargetPD.GetCellData().GetScalars().Modified();
		vTargetPD.Modified();

		vTargetGF = VtkUtil.formGeometryFilter(vTargetPD, numPts);

		vtkPolyDataMapper vTargetRegPDM = new vtkPolyDataMapper();
		vTargetRegPDM.SetScalarModeToUseCellData();
		vTargetRegPDM.SetInputConnection(vTargetGF.GetOutputPort());

		vTargetA = new VtkLodActor(this);
		vTargetA.setDefaultMapper(vTargetRegPDM);
		vTargetA.setLodMapper(LodMode.MaxQuality, vTargetRegPDM);

		vtkPolyDataMapper vTargetDecPDM = LodUtil.createQuadricDecimatedMapper(vTargetGF.GetOutputPort());
		vTargetA.setLodMapper(LodMode.MaxSpeed, vTargetDecPDM);

		vTargetA.GetProperty().SetPointSize(2.0f);
	}

	/**
	 * Helper method to update the VTK state associated with the colors of the
	 * lidar data.
	 */
	private void doUpdateStateColor(ColorProvider aSourceCP, ColorProvider aTargetCP)
	{
		// Update cache
		cSourceCP = aSourceCP;
		cTargetCP = aTargetCP;
		if (cSourceCP == null || cTargetCP == null)
			return;

		// Update the colors associated with the source points
		Color tmpColorA = aSourceCP.getColor(0.0, 1.0, 0.5);
		double r = tmpColorA.getRed() / 255.0;
		double g = tmpColorA.getGreen() / 255.0;
		double b = tmpColorA.getBlue() / 255.0;
		vSourceA.GetProperty().SetColor(r, g, b);
		vSourceA.Modified();

		// Update the colors associated with the target points
		FeatureType tmpFT = aTargetCP.getFeatureType();
		FeatureAttr tmpFA = getFeatureAttrFor(tmpFT);

		double minVal = tmpFA.getMinVal();
		double maxVal = tmpFA.getMaxVal();
		int numPts = tmpFA.getNumVals();
		for (int aIdx = 0; aIdx < numPts; aIdx++)
		{
			double tmpVal = tmpFA.getValAt(aIdx);
			Color tmpColorB = aTargetCP.getColor(minVal, maxVal, tmpVal);
			vColorUCA.SetTuple4(aIdx, tmpColorB.getRed(), tmpColorB.getGreen(), tmpColorB.getBlue(), tmpColorB.getAlpha());
		}
		vColorUCA.Modified();
	}

	/**
	 * Helper method to update the VTK state associated with the translation of
	 * the lidar data.
	 *
	 * @param aTranslationV
	 */
	private void doUpdateStateTranslation(Vector3D aTranslationV, double aRadialOffset)
	{
		// Update cache
		cTranslationV = aTranslationV;
		cRadialOffset = aRadialOffset;

		// Update the VTK state for each target lidar point position
		int numberOfPoints = getNumberOfPoints();
		for (int aIdx = 0; aIdx < numberOfPoints; aIdx++)
		{
			Vector3D tmpPos = refManager.getTargetPosition(refItem, aIdx);
			tmpPos = LidarGeoUtil.transformTarget(aTranslationV, aRadialOffset, tmpPos);

			vTargetP.SetPoint(aIdx, tmpPos.getX(), tmpPos.getY(), tmpPos.getZ());
		}
		vTargetP.Modified();
	}

	/**
	 * Helper method that returns the number of points in this object
	 */
	private int getNumberOfPoints()
	{
		return timeFA.getNumVals();
	}

}
