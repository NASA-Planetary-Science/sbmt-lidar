package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.LidarDataSource;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PointInCylinderChecker;
import edu.jhuapl.saavtk.model.PointInRegionChecker;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.lidar.LidarSearchParms;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.util.LidarQueryUtil;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Panel used to provide a search for lidar tracks capability.
 */
public class LidarSearchPanel extends JPanel
		implements ActionListener, ChangeListener, ItemListener, PickManagerListener, ItemEventListener
{
	// Reference vars
	private final ModelManager refModelManager;
	protected final PolyhedralModel refSmallBodyModel;
	private final BodyViewConfig refBodyViewConfig;
	private final LidarTrackManager refTrackManager;
	private final PickManager refPickManager;
	private final Picker refPicker;

	// State vars
	private LidarSearchParms cSearchParms;

	// GUI vars
	private JPanel searchPropertiesPanel;
	private JComboBox<LidarDataSource> sourceBox;
	private JButton manageSourcesB;
	private JSpinner begTimeSpinner;
	private JSpinner endTimeSpinner;
	private JFormattedTextField minTrackSizeTF;
	private JFormattedTextField trackSeparationTF;
	private JFormattedTextField minSCRange;
	private JFormattedTextField maxSCRange;
	private JToggleButton selectRegionB;
	private JButton clearRegionB;
	private JButton searchB;

	/**
	 * Constructor
	 */
	public LidarSearchPanel(BodyViewConfig aBodyViewConfig, ModelManager aModelManager, PickManager aPickManager,
			LidarTrackManager aTrackManager)
	{
		refModelManager = aModelManager;
		refSmallBodyModel = aModelManager.getPolyhedralModel();
		refBodyViewConfig = aBodyViewConfig;
		refTrackManager = aTrackManager;
		refPickManager = aPickManager;
		refPicker = refPickManager.getPickerForPickMode(PickMode.CIRCLE_SELECTION);

		cSearchParms = null;

		formSearchPanel();

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, refPicker, this);
		refPickManager.addListener(this);
		refTrackManager.addListener(this);
		begTimeSpinner.addChangeListener(this);
		endTimeSpinner.addChangeListener(this);

		// Initialize the sourceBox
		updateSourceBox();
	}

	/**
	 * Method that performs the actual query for lidar tracks when the submit
	 * action is triggered.
	 */
	protected void handleActionSubmit(LidarDataSource aDataSource, AbstractEllipsePolygonModel aSelectionRegion)
	{
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		TreeSet<Integer> cubeList = null;

		if (aSelectionRegion.getNumItems() > 0)
		{
			int numberOfSides = aSelectionRegion.getNumberOfSides();
			Ellipse region = aSelectionRegion.getItem(0);

			// Always use the lowest resolution model for getting the intersection
			// cubes list. Therefore, if the selection region was created using a
			// higher resolution model, we need to recompute the selection region
			// using the low res model.
			if (refSmallBodyModel.getModelResolution() > 0)
			{
				vtkPolyData interiorPoly = new vtkPolyData();
				refSmallBodyModel.drawRegularPolygonLowRes(region.getCenter().toArray(), region.getRadius(), numberOfSides,
						interiorPoly, null);
				cubeList = refSmallBodyModel.getIntersectingCubes(new BoundingBox(interiorPoly.GetBounds()));
			}
			else
			{
				cubeList = refSmallBodyModel.getIntersectingCubes(new BoundingBox(aSelectionRegion.getVtkInteriorPolyDataFor(region).GetBounds()));
			}
		}
		else
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
					"Please select a region on the asteroid.", "Error", JOptionPane.ERROR_MESSAGE);
			setCursor(Cursor.getDefaultCursor());

			return;
		}
		showData(cubeList, aSelectionRegion);
		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Helper method that will populate the sourceBox with the available data
	 * sources.
	 */
	protected void updateSourceBox()
	{
//		PolyhedralModel refSmallBodyModel = refModelManager.getPolyhedralModel();
		// if (smallBodyModel.getLidarDatasourceIndex() < 0)
		// smallBodyModel.setLidarDatasourceIndex(0);

		// Retrieve the set of available lidar sources
		Map<String, LidarDataSource> sourceM = LidarQueryUtil.getLidarDataSourceMap(refSmallBodyModel);
		LinkedHashSet<LidarDataSource> tmpSourceS = new LinkedHashSet<>();

		// Add the remove server data sources
		if (sourceM.containsKey("Default"))
			tmpSourceS.add(sourceM.get("Default"));

		for (LidarDataSource aSource : sourceM.values())
			tmpSourceS.add(aSource);

		// Add the local custom data sources
		for (LidarDataSource aDataSource : refSmallBodyModel.getLidarDataSourceList())
			tmpSourceS.add(aDataSource);

		// Save off the current source selection
		LidarDataSource pickSource = (LidarDataSource) sourceBox.getSelectedItem();

		// Update the sourceBox
		sourceBox.removeItemListener(this);
		sourceBox.removeAllItems();

		for (LidarDataSource aSource : tmpSourceS)
			sourceBox.addItem(aSource);

		// Select the previously selected item
		List<LidarDataSource> tmpSourceL = new ArrayList<>(tmpSourceS);
		int pickIdx = tmpSourceL.indexOf(pickSource);
		if (pickIdx != -1)
			sourceBox.setSelectedIndex(pickIdx);

		sourceBox.addItemListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == selectRegionB)
			doActionSelectRegion();
		else if (source == clearRegionB)
			doActionClearRegion();
		else if (source == manageSourcesB)
			doActionManageSources();
		else if (source == searchB)
			doActionSubmit((LidarDataSource) sourceBox.getSelectedItem());
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		// Clear the cached SearchParms whenever the TrackManager's list of Tracks
		// are changed. This allows for a new 'search' query to be executed.
		if (aEventType == ItemEventType.ItemsChanged)
			cSearchParms = null;
	}

	@Override
	public void itemStateChanged(ItemEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == sourceBox)
		{
			String sourceName = ((LidarDataSource) sourceBox.getSelectedItem()).getName();

			// Update the valid time range
			Date begTime = refBodyViewConfig.lidarSearchDefaultStartDate;
			Date endTime = refBodyViewConfig.lidarSearchDefaultStartDate;
			try
			{
				begTime = refBodyViewConfig.orexSearchTimeMap.get(sourceName).get(0);
				endTime = refBodyViewConfig.orexSearchTimeMap.get(sourceName).get(1);
			}
			catch (Exception aExp)
			{
				aExp.printStackTrace();
			}

			updateSpinnerTimeRange(begTimeSpinner, begTime, endTime, begTime);
			updateSpinnerTimeRange(endTimeSpinner, begTime, endTime, endTime);
		}
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = refPicker == refPickManager.getActivePicker();
		selectRegionB.setSelected(tmpBool);
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		updateGui();
	}

	/**
	 * Helper method that handles the clear region action.
	 */
	private void doActionClearRegion()
	{
		AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) refModelManager
				.getModel(ModelNames.CIRCLE_SELECTION);
		selectionModel.removeAllStructures();
	}

	/**
	 * Helper method that handles the manage sources action.
	 */
	private void doActionManageSources()
	{
		CustomLidarDataDialog dialog = new CustomLidarDataDialog(refModelManager);
		dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this));
		dialog.setVisible(true);

		// update the panel to reflect changes to the lidar datasources
		if (refSmallBodyModel.getLidarDataSourceList().size() > 0)
			refSmallBodyModel.loadCustomLidarDataSource();
		updateSourceBox();
	}

	/**
	 * Helper method that handles the select region action.
	 */
	private void doActionSelectRegion()
	{
		Picker targPicker = null;
		if (selectRegionB.isSelected() == true)
			targPicker = refPicker;

		refPickManager.setActivePicker(targPicker);
	}

	/**
	 * Helper method that handles the submit action.
	 */
	private void doActionSubmit(LidarDataSource aDataSource)
	{
		refPickManager.setActivePicker(null);

		AbstractEllipsePolygonModel selectionRegion = (AbstractEllipsePolygonModel) refModelManager
				.getModel(ModelNames.CIRCLE_SELECTION);
		// Delegate actual query submission
		handleActionSubmit(aDataSource, selectionRegion);
	}

	/**
	 * Helper method that forms the "Search" panel.
	 */
	private void formSearchPanel()
	{
		setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel sourcePanel = new JPanel();
		add(sourcePanel);
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));

		JLabel lblNewLabel = new JLabel("Source:");
		sourcePanel.add(lblNewLabel);

		sourceBox = new JComboBox<>();
		sourceBox.setModel(new DefaultComboBoxModel<>(new LidarDataSource[] { LidarDataSource.Invalid }));
		sourceBox.setRenderer(new DataSourceRenderer());
		sourcePanel.add(sourceBox);

		manageSourcesB = new JButton("Manage");
		manageSourcesB.addActionListener(this);
		sourcePanel.add(manageSourcesB);

		JPanel timeRangePanel = new JPanel();
		timeRangePanel.setBorder(null);
		add(timeRangePanel);
		timeRangePanel.setLayout(new BoxLayout(timeRangePanel, BoxLayout.X_AXIS));

		JLabel lblStart = new JLabel("Start:");
		timeRangePanel.add(lblStart);

		// Initialize the begDate, endDate spinners
		begTimeSpinner = new JSpinner();
		Date begDate = new Date(951714000000L);
		Date endDate = new Date(982040400000L);
		if (refBodyViewConfig.hasLidarData == true)
		{
			begDate = refBodyViewConfig.lidarSearchDefaultStartDate;
			endDate = refBodyViewConfig.lidarSearchDefaultEndDate;
		}

		begTimeSpinner.setModel(new SpinnerDateModel(begDate, null, null, Calendar.DAY_OF_MONTH));
		begTimeSpinner.setEditor(new JSpinner.DateEditor(begTimeSpinner, "yyyy-MMM-dd HH:mm:ss"));
		begTimeSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, begTimeSpinner.getPreferredSize().height));
		timeRangePanel.add(begTimeSpinner);

		JLabel lblEnd = new JLabel("End:");
		timeRangePanel.add(lblEnd);

		endTimeSpinner = new JSpinner();
		endTimeSpinner.setModel(new SpinnerDateModel(endDate, null, null, Calendar.DAY_OF_MONTH));
		endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "yyyy-MMM-dd HH:mm:ss"));
		endTimeSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, endTimeSpinner.getPreferredSize().height));
		timeRangePanel.add(endTimeSpinner);

// TODO: Move this to formSearchPanel()....
//			if (refBodyViewConfig.hasLidarData == true)
//			{
//				((SpinnerDateModel) begTimeSpinner.getModel()).setValue(refBodyViewConfig.lidarSearchDefaultStartDate);
//				((SpinnerDateModel) endTimeSpinner.getModel()).setValue(refBodyViewConfig.lidarSearchDefaultEndDate);
//			}

		Component verticalStrut = Box.createVerticalStrut(20);
		add(verticalStrut);

		searchPropertiesPanel = new JPanel();
		searchPropertiesPanel.setBorder(null);
		add(searchPropertiesPanel);
		GridBagLayout gbl_searchPropertiesPanel = new GridBagLayout();
		gbl_searchPropertiesPanel.columnWidths = new int[] { 95, 30, 30, 0, 75, 0, 0, 30, 30 };
		gbl_searchPropertiesPanel.rowHeights = new int[] { 26, 0, 0, 0, 0 };
		gbl_searchPropertiesPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_searchPropertiesPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		searchPropertiesPanel.setLayout(gbl_searchPropertiesPanel);

		JLabel lblMinTrackSize = new JLabel("Min Track Size:");
		GridBagConstraints gbc_lblMinTrackSize = new GridBagConstraints();
		gbc_lblMinTrackSize.anchor = GridBagConstraints.WEST;
		gbc_lblMinTrackSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinTrackSize.gridx = 0;
		gbc_lblMinTrackSize.gridy = 0;
		searchPropertiesPanel.add(lblMinTrackSize, gbc_lblMinTrackSize);

		minTrackSizeTF = new JFormattedTextField();
		minTrackSizeTF.setText("10");
		minTrackSizeTF.setMaximumSize(new Dimension(Integer.MAX_VALUE, minTrackSizeTF.getPreferredSize().height));

		GridBagConstraints gbc_minTrackSizeTextField = new GridBagConstraints();
		gbc_minTrackSizeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minTrackSizeTextField.gridwidth = 2;
		gbc_minTrackSizeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minTrackSizeTextField.gridx = 1;
		gbc_minTrackSizeTextField.gridy = 0;
		searchPropertiesPanel.add(minTrackSizeTF, gbc_minTrackSizeTextField);

		JLabel lblNewLabel_1 = new JLabel("Track Separation (sec):");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 3;
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 4;
		gbc_lblNewLabel_1.gridy = 0;
		searchPropertiesPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		trackSeparationTF = new JFormattedTextField();
		trackSeparationTF.setText("10");
		trackSeparationTF.setMaximumSize(new Dimension(Integer.MAX_VALUE, trackSeparationTF.getPreferredSize().height));

		GridBagConstraints gbc_trackSeparationTextField = new GridBagConstraints();
		gbc_trackSeparationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_trackSeparationTextField.gridwidth = 2;
		gbc_trackSeparationTextField.insets = new Insets(0, 0, 5, 0);
		gbc_trackSeparationTextField.gridx = 7;
		gbc_trackSeparationTextField.gridy = 0;
		searchPropertiesPanel.add(trackSeparationTF, gbc_trackSeparationTextField);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		add(verticalStrut_1);

		JPanel searchButtonsPanel = new JPanel();
		add(searchButtonsPanel);

		searchButtonsPanel.setLayout(new BoxLayout(searchButtonsPanel, BoxLayout.X_AXIS));

		selectRegionB = new JToggleButton("Select Region");
		selectRegionB.addActionListener(this);
		searchButtonsPanel.add(selectRegionB);

		clearRegionB = new JButton("Clear Region");
		clearRegionB.addActionListener(this);
		searchButtonsPanel.add(clearRegionB);

		searchB = new JButton("Search");
		searchB.addActionListener(this);
		searchButtonsPanel.add(searchB);
	}

	/**
	 * Helper method that adds the 'Spacecraft Range' UI section
	 */
	protected void addSpacecraftRangeArea()
	{
		JLabel lblSpacecraftRange = new JLabel("Spacecraft Range:");
		GridBagConstraints gbc_lblSpacecraftRange = new GridBagConstraints();
		gbc_lblSpacecraftRange.anchor = GridBagConstraints.WEST;
		gbc_lblSpacecraftRange.insets = new Insets(0, 0, 0, 5);
		gbc_lblSpacecraftRange.gridx = 0;
		gbc_lblSpacecraftRange.gridy = 2;
		searchPropertiesPanel.add(lblSpacecraftRange, gbc_lblSpacecraftRange);

		minSCRange = new JFormattedTextField();
		GridBagConstraints gbc_minSCRange = new GridBagConstraints();
		gbc_minSCRange.fill = GridBagConstraints.HORIZONTAL;
		gbc_minSCRange.gridwidth = 2;
		gbc_minSCRange.insets = new Insets(0, 0, 5, 5);
		gbc_minSCRange.gridx = 1;
		gbc_minSCRange.gridy = 2;
		searchPropertiesPanel.add(minSCRange, gbc_minSCRange);
		minSCRange.setText("0");

		JLabel lblTo = new JLabel("to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.gridx = 3;
		gbc_lblTo.gridy = 2;
		searchPropertiesPanel.add(lblTo, gbc_lblTo);

		maxSCRange = new JFormattedTextField();
		maxSCRange.setText("500");
		GridBagConstraints gbc_maxSCRange = new GridBagConstraints();
		gbc_maxSCRange.fill = GridBagConstraints.BOTH;
		gbc_maxSCRange.insets = new Insets(0, 0, 5, 5);
		gbc_maxSCRange.gridx = 4;
		gbc_maxSCRange.gridy = 2;
		searchPropertiesPanel.add(maxSCRange, gbc_maxSCRange);

		JLabel lblKm = new JLabel("km");
		GridBagConstraints gbc_lblKm = new GridBagConstraints();
		gbc_lblKm.insets = new Insets(0, 0, 5, 5);
		gbc_lblKm.gridx = 5;
		gbc_lblKm.gridy = 2;
		searchPropertiesPanel.add(lblKm, gbc_lblKm);
	}

	/**
	 * Helper method that performs the actual search query and installs the
	 * resulting Tracks into aTrackManager.
	 */
	protected void executeQuery(LidarTrackManager aTrackManager, LidarSearchParms aSearchParms,
			PointInRegionChecker aPointInRegionChecker, double aTimeSeparationBetweenTracks, int aMinTrackLength)
			throws IOException
	{
		// Bail if this query has already been executed
		if (aSearchParms.equals(cSearchParms) == true)
			return;

		// Delegate
		LidarQueryUtil.executeQueryClassic(aTrackManager, aSearchParms, aPointInRegionChecker);

		// Cache query so that redundant requests can be ignored.
		cSearchParms = aSearchParms;
	}

	/**
	 * Returns the space craft range constraint (as a 2 element array).
	 */
	protected double[] getSpaceCraftRangeConstraint()
	{
		if (minSCRange == null || maxSCRange == null)
			return null;

		double minVal = Double.parseDouble(minSCRange.getText());
		double maxVal = Double.parseDouble(maxSCRange.getText());
		return new double[] { minVal, maxVal };
	}

	/**
	 * Returns the time constraint (as a 2 element array).
	 */
	protected double[] getTimeConstraint()
	{
		double begTime = getTime(begTimeSpinner);
		double endTime = getTime(endTimeSpinner);
		return new double[] { begTime, endTime };
	}

	// TODO: Add comments
	protected void showData(TreeSet<Integer> aCubeList, AbstractEllipsePolygonModel aSelectionRegion)
	{
		int minTrackLength = Integer.parseInt(minTrackSizeTF.getText());
		if (minTrackLength < 1)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
					"Minimum track length must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		double timeSeparationBetweenTracks = Double.parseDouble(trackSeparationTF.getText());
		if (timeSeparationBetweenTracks < 0.0)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "Track separation must be nonnegative.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		LidarDataSource dataSource = (LidarDataSource) sourceBox.getSelectedItem();

		// Range Constraints
		double minRange = Double.NaN;
		double maxRange = Double.NaN;
		if (minSCRange != null && maxSCRange != null)
		{
			minRange = Double.parseDouble(minSCRange.getText());
			maxRange = Double.parseDouble(maxSCRange.getText());
		}

		// Time constraints
		double begTime = getTime(begTimeSpinner);
		double endTime = getTime(endTimeSpinner);

		// Region constraints
		PointInCylinderChecker checker = null;
		if (aSelectionRegion.getNumItems() > 0)
		{
			Ellipse region = aSelectionRegion.getItem(0);
			if (region.getRadius() > 0.0)
				checker = new PointInCylinderChecker(refModelManager.getPolyhedralModel(), region.getCenter().toArray(),
						region.getRadius());
		}

		// Search parameters
		LidarSearchParms tmpSearchParms = new LidarSearchParms(dataSource, begTime, endTime, minRange, maxRange,
				timeSeparationBetweenTracks, minTrackLength, aCubeList);

		// Note picking functionality is being toggled
		PickUtil.setPickingEnabled(false);

		// Execute the query
		try
		{
			executeQuery(refTrackManager, tmpSearchParms, checker, timeSeparationBetweenTracks, minTrackLength);
		}
		catch (IOException aEvent)
		{
			aEvent.printStackTrace();
		}

		PickUtil.setPickingEnabled(true);
	}

	/**
	 * Updates the various UI elements to keep them synchronized
	 */
	private void updateGui()
	{
		// TODO: Finish logic here
	}

	/**
	 * Helper method to update the time range for the specified JSpinner.
	 *
	 * @param aSpinner
	 * @param aBegTime
	 * @param aEndTime
	 */
	private void updateSpinnerTimeRange(JSpinner aSpinner, Date aBegTime, Date aEndTime, Date aCurrTime)
	{
		aSpinner.removeChangeListener(this);

		SpinnerDateModel tmpModel = (SpinnerDateModel) aSpinner.getModel();
		Date currTime = tmpModel.getDate();

		// Restrict currTime to the valid time range
		if (aBegTime != null && currTime.before(aBegTime) == true)
			currTime = aBegTime;
		if (aEndTime != null && currTime.after(aEndTime) == true)
			currTime = aEndTime;

		// Update the model to reflect the valid time range
		tmpModel.setStart(aBegTime);
		tmpModel.setEnd(aEndTime);
		// TODO: Do we need aCurrTime vs currTime
		tmpModel.setValue(aCurrTime);
//		tmpModel.setValue(currTime);

		aSpinner.addChangeListener(this);
	}

	/**
	 * Utility helper method that returns the current time associated with the
	 * specified time spinner.
	 */
	private static double getTime(JSpinner aTimeSpinner)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

		Date tmpDate = ((SpinnerDateModel) aTimeSpinner.getModel()).getDate();

		double retTime = Double.NaN;
		if (tmpDate != null)
			retTime = TimeUtil.str2et(sdf.format(tmpDate).replace(' ', 'T'));

		return retTime;
	}

}
