package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import com.google.common.base.Stopwatch;

import vtk.vtkCubeSource;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.LidarDatasourceInfo;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PointInCylinderChecker;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.lidar.LidarPopupMenu;
import edu.jhuapl.sbmt.gui.lidar.v2.LidarSearchController;
import edu.jhuapl.sbmt.model.lidar.LaserLidarHyperTreeSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

public class Hayabusa2LaserLidarHyperTreeSearchPanel extends LidarSearchController//LidarSearchPanel  // currently implemented only for OLA lidar points, but could be revised to handle any points satisfying the LidarPoint interface.
{
    Renderer renderer;

    public Hayabusa2LaserLidarHyperTreeSearchPanel(SmallBodyViewConfig smallBodyConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer)
    {
        super(smallBodyConfig, modelManager, pickManager, renderer);
        this.renderer=renderer;
        this.view = new Hayabusa2LidarSearchView();
        setupConnections();
    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.LIDAR_HYPERTREE_SEARCH;
    }

    public void updateLidarDatasourceComboBox()
    {
        super.updateLidarDatasourceComboBox();

        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        LaserLidarHyperTreeSearchDataCollection lidarHyperTreeSearchDataCollection = (LaserLidarHyperTreeSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(getLidarModelName());

        // clear the skeletons instances (should we try to keep these around to avoid having to load them again? -turnerj1)
        lidarHyperTreeSearchDataCollection.clearDatasourceSkeletons();

        // add the server datasource
        String defaultDatasourceName = "Default";
        String defaultDatasourcePath = lidarModel.getLidarDataSourceMap().get("Hayabusa2");
        lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(defaultDatasourceName, defaultDatasourcePath);

        // add the custom local datasources
        for (LidarDatasourceInfo info : smallBodyModel.getLidarDasourceInfoList())
        {
            String datasourceName = info.name;
            String datasourcePath = info.path;
            lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(datasourceName, datasourcePath);
        }

        // set the current datasource
        int index = smallBodyModel.getLidarDatasourceIndex();
        String datasourceName = smallBodyModel.getLidarDatasourceName(index);
        lidarHyperTreeSearchDataCollection.setCurrentDatasourceSkeleton(datasourceName);
    }

    @Override
    protected void submitButtonActionPerformed(ActionEvent evt)
    {
        lidarModel.removePropertyChangeListener(propertyChangeListener);

        view.getSelectRegionButton().setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);

        //int lidarIndex = smallBodyModel.getLidarDatasourceIndex();
        //String lidarDatasourceName = smallBodyModel.getLidarDatasourceName(lidarIndex);
        //String lidarDatasourcePath = smallBodyModel.getLidarDatasourcePath(lidarIndex);
        int lidarIndex=-1;
        String lidarDatasourceName="Hayabusa2";
        String lidarDatasourcePath=lidarModel.getLidarDataSourceMap().get(lidarDatasourceName);
        System.out.println("Current Lidar Datasource Index : " + lidarIndex);
        System.out.println("Current Lidar Datasource Name: " + lidarDatasourceName);
        System.out.println("Current Lidar Datasource Path: " + lidarDatasourcePath);

        // read in the skeleton, if it hasn't been read in already
        ((LaserLidarHyperTreeSearchDataCollection)lidarModel).readSkeleton();

        Hayabusa2LidarHypertreeSkeleton skeleton = ((LaserLidarHyperTreeSearchDataCollection)lidarModel).getCurrentSkeleton();

        double[] selectionRegionCenter = null;
        double selectionRegionRadius = 0.0;

        AbstractEllipsePolygonModel.EllipsePolygon region=null;
        vtkPolyData interiorPoly=new vtkPolyData();
        if (selectionModel.getNumberOfStructures() > 0)
        {
            region=(AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);
            selectionRegionCenter = region.center;
            selectionRegionRadius = region.radius;


            // Always use the lowest resolution model for getting the intersection cubes list.
            // Therefore, if the selection region was created using a higher resolution model,
            // we need to recompute the selection region using the low res model.
            if (smallBodyModel.getModelResolution() > 0)
                smallBodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);    // this sets interiorPoly
            else
                interiorPoly=region.interiorPolyData;

        }
        else
        {
            vtkCubeSource box=new vtkCubeSource();
            double[] bboxBounds=smallBodyModel.getBoundingBox().getBounds();
            BoundingBox bbox=new BoundingBox(bboxBounds);
            bbox.increaseSize(0.01);
            box.SetBounds(bbox.getBounds());
            box.Update();
            interiorPoly.DeepCopy(box.GetOutput());
        }


        System.out.println("Found matching lidar data path: "+lidarDatasourcePath);
        lidarModel.addPropertyChangeListener(propertyChangeListener);
        view.getRadialOffsetSlider().setModel(lidarModel);
        view.getRadialOffsetSlider().setOffsetScale(lidarModel.getOffsetScale());
        lidarPopupMenu = new LidarPopupMenu(lidarModel, renderer);

        Stopwatch sw=new Stopwatch();
        sw.start();



        double[] bounds = interiorPoly.GetBounds();
        double[] rangeLims = new double[] {Double.parseDouble(((Hayabusa2LidarSearchView)view).getMinSCRange().getText()), Double.parseDouble(((Hayabusa2LidarSearchView)view).getMaxSCRange().getText())};
        TreeSet<Integer> cubeList=((LaserLidarHyperTreeSearchDataCollection)lidarModel).getLeavesIntersectingBoundingBox(new BoundingBox(bounds), getSelectedTimeLimits(), rangeLims);
        System.out.println(cubeList);
        System.out.println("Search Time="+sw.elapsedMillis()+" ms");
        sw.stop();


        Picker.setPickingEnabled(false);

        ((LaserLidarHyperTreeSearchDataCollection)lidarModel).setParentForProgressMonitor(view);
        showData(cubeList, selectionRegionCenter, selectionRegionRadius);
        view.getRadialOffsetSlider().reset();


        Picker.setPickingEnabled(true);

    }

    @Override
    protected void showData(
            TreeSet<Integer> cubeList,
            double[] selectionRegionCenter,
            double selectionRegionRadius)
    {
        int minTrackLength = Integer.parseInt(view.getMinTrackSizeTextField().getText());
        if (minTrackLength < 1)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view),
                    "Minimum track length must be a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        double timeSeparationBetweenTracks = Double.parseDouble(view.getTrackSeparationTextField().getText());
        if (timeSeparationBetweenTracks < 0.0)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view),
                    "Track separation must be nonnegative.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        double minRange = Double.parseDouble(((Hayabusa2LidarSearchView)view).getMinSCRange().getText());
        double maxRange = Double.parseDouble(((Hayabusa2LidarSearchView)view).getMaxSCRange().getText());

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            double start = TimeUtil.str2et(sdf.format(model.getStartDate()).replace(' ', 'T'));
            double end = TimeUtil.str2et(sdf.format(model.getEndDate()).replace(' ', 'T'));
            PointInCylinderChecker checker;
            if (selectionRegionCenter==null || selectionRegionRadius<=0)// do time-only search
                checker=null;
            else
                checker=new PointInCylinderChecker(modelManager.getPolyhedralModel(), selectionRegionCenter, selectionRegionRadius);
            //
            lidarModel.setLidarData(
                    view.getSourceComboBox().getSelectedItem().toString(),
                    start,
                    end,
                    cubeList,
                    checker,
                    timeSeparationBetweenTracks,
                    minTrackLength,
                    minRange,
                    maxRange);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }


}
