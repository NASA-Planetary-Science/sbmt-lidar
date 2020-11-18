package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.gui.ColorMode;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.gui.action.LidarGuiUtil;
import edu.jhuapl.sbmt.lidar.gui.color.ColorConfigPanel;
import edu.jhuapl.sbmt.lidar.util.LidarGeoUtil;

import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
import glum.gui.component.GSlider;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.NumberRenderer;
import glum.gui.table.PrePendRenderer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemGroup;
import glum.item.ItemManagerUtil;
import glum.text.SigFigNumberFormat;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of lidar Tracks.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Display list of tracks in a table
 * <LI>Allow user to show, hide, or remove tracks
 * <LI>Allow user to drag or manually translate tracks
 * </UL>
 *
 * @author lopeznr1
 */
public class LidarTrackPanel extends JPanel
		implements ActionListener, ChangeListener, ItemEventListener, PickListener, PickManagerListener
{
	// Ref vars
	private final LidarTrackManager refTrackManager;
	private final PickManager refPickManager;

	// State vars
	private final LidarShiftPicker lidarPicker;
	private double radialOffsetScale;

	// GUI vars
	private final PopupMenu<?> popupMenu;
	private LidarTrackTranslateDialog translateDialog;
	private LidarSaveDialog saveDialog;
	private ItemListPanel<LidarTrack> lidarILP;
	private JLabel titleL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton hideB, showB;
	private JButton removeB, saveB;
	private ColorConfigPanel<?> colorConfigPanel;
	private JButton translateB;
	private JToggleButton dragB;

	private JCheckBox showErrorCB;
	private JComboBox<ItemGroup> errorModeBox;
	private JLabel errorModeL, errorValueL;
	private GSlider radialS;
	private JButton radialResetB;
	private JCheckBox showSpacecraftCB;
	private JSpinner pointSizeSpinner;

	/** Standard Constructor */
	public LidarTrackPanel(LidarTrackManager aTrackManager, PickManager aPickManager, Renderer aRenderer,
			PolyhedralModel aSmallBody)
	{
		refTrackManager = aTrackManager;
		refPickManager = aPickManager;

		lidarPicker = new LidarShiftPicker(refTrackManager, aRenderer, aSmallBody);
		radialOffsetScale = LidarGeoUtil.getOffsetScale(refTrackManager);

		setLayout(new MigLayout());

		// Popup menu
		popupMenu = LidarGuiUtil.formLidarTrackPopupMenu(refTrackManager, aRenderer);

		// Table header
		selectInvertB = GuiUtil.formButton(this, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(this, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(this, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		titleL = new JLabel("Tracks: ---");
		add(titleL, "growx,span,split");
		add(selectInvertB, "w 24!,h 24!");
		add(selectNoneB, "w 24!,h 24!");
		add(selectAllB, "w 24!,h 24!,wrap 2");

		// Table Content
		QueryComposer<LookUp> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(LookUp.IsVisible, Boolean.class, "Show", null);
		tmpComposer.addAttribute(LookUp.Color, ColorProvider.class, "Color", null);
		tmpComposer.addAttribute(LookUp.Name, String.class, "Track", null);
		tmpComposer.addAttribute(LookUp.NumPoints, Integer.class, "# pts", null);
		tmpComposer.addAttribute(LookUp.BegTime, Double.class, "Start Time", null);
		tmpComposer.addAttribute(LookUp.EndTime, Double.class, "End Time", null);
		tmpComposer.addAttribute(LookUp.Source, String.class, "Source", null);

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);
		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setEditor(LookUp.Color, new ColorProviderCellEditor<>());
		tmpComposer.setRenderer(LookUp.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setRenderer(LookUp.Name, new PrePendRenderer("Trk "));
		tmpComposer.setRenderer(LookUp.NumPoints, new NumberRenderer("###,###,###", "---"));
		tmpComposer.setRenderer(LookUp.BegTime, tmpTimeRenderer);
		tmpComposer.setRenderer(LookUp.EndTime, tmpTimeRenderer);

		ItemHandler<LidarTrack> tmpIH = new TrackItemHandler(refTrackManager, tmpComposer);
		ItemProcessor<LidarTrack> tmpIP = refTrackManager;
		lidarILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		lidarILP.setSortingEnabled(true);

		JTable lidarTable = lidarILP.getTable();
		lidarTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		lidarTable.addMouseListener(new TablePopupHandler(refTrackManager, popupMenu));
		add(new JScrollPane(lidarTable), "growx,growy,pushx,pushy,span,wrap");

		// Action buttons: hide / show
		hideB = GuiUtil.formButton(this, "Hide");
		hideB.setToolTipText("Hide Tracks");
		showB = GuiUtil.formButton(this, "Show");
		showB.setToolTipText("Show Tracks");

		// Action buttons: drag / translate
		dragB = new JToggleButton("Drag");
		dragB.addActionListener(this);
		dragB.setToolTipText("Drag Tracks");
		translateB = GuiUtil.formButton(this, "Translate");
		translateB.setToolTipText("Translate Tracks");

		// Action buttons: remove / save
		removeB = GuiUtil.formButton(this, "Remove");
		removeB.setToolTipText("Remove Tracks");
		saveB = GuiUtil.formButton(this, "Save");
		saveB.setToolTipText("Save Tracks");

		// Radial offset section
		Range<Double> tmpRange = Range.closed(-15.0, 15.0);
		radialS = new GSlider(this, tmpRange, 30);
		radialS.setMajorTickSpacing(5);
		radialS.setMinorTickSpacing(1);
		radialS.setPaintTicks(true);
		radialS.setSnapToTicks(true);
		radialS.setModelValue(0.0);
		radialResetB = GuiUtil.formButton(this, "Reset");

		// Show spacecraft checkbox
		showSpacecraftCB = new JCheckBox("Show spacecraft position");
		showSpacecraftCB.setSelected(false);
		showSpacecraftCB.addActionListener(this);

		// Form the left and right sub panels
		JPanel leftPanel = formLeftPanel();
		add(leftPanel, "growx,growy,pushx");

		add(GuiUtil.createDivider(), "growy,w 4!");

		colorConfigPanel = new ColorConfigPanel<>(this, refTrackManager, aRenderer);
		colorConfigPanel.setActiveMode(ColorMode.AutoHue);
		add(colorConfigPanel, "ax right,ay top,growx,wrap");

		// Point size section
		JLabel tmpL = new JLabel("Point Size:");
		pointSizeSpinner = new JSpinner();
		pointSizeSpinner.addChangeListener(this);
		pointSizeSpinner.setModel(new SpinnerNumberModel(2, 1, 100, 1));
		add(tmpL, "span,split");
		add(pointSizeSpinner, "growx,wrap");

		// Error section
		showErrorCB = new JCheckBox("Show Error:");
		showErrorCB.addActionListener(this);
		showErrorCB.setToolTipText(
				"<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");
		errorValueL = new JLabel("");
		add(showErrorCB, "span,split");
		add(errorValueL, "growx,w 0::");

		ItemGroup[] errorModeArr = { ItemGroup.All, ItemGroup.Visible, ItemGroup.Selected };
		errorModeL = new JLabel("Mode:");
		errorModeBox = new JComboBox<>(errorModeArr);
		errorModeBox.setSelectedItem(ItemGroup.Visible);
		errorModeBox.addActionListener(this);
		add(errorModeL, "");
		add(errorModeBox, "");

		updateGui();
		updateErrorUI();
		configureColumnWidths();

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, lidarPicker, this);
		refTrackManager.addListener(this);
		refPickManager.addListener(this);
		refPickManager.getDefaultPicker().addListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		List<LidarTrack> tmpL = refTrackManager.getSelectedItems().asList();
		if (source == selectAllB)
			ItemManagerUtil.selectAll(refTrackManager);
		else if (source == selectNoneB)
			ItemManagerUtil.selectNone(refTrackManager);
		else if (source == selectInvertB)
			ItemManagerUtil.selectInvert(refTrackManager);
		else if (source == hideB)
			refTrackManager.setIsVisible(tmpL, false);
		else if (source == showB)
			refTrackManager.setIsVisible(tmpL, true);
		else if (source == removeB)
			refTrackManager.removeItems(tmpL);
		else if (source == translateB)
			doActionTranslate(tmpL);
		else if (source == dragB)
			doActionDrag();
		else if (source == saveB)
			doActionSave();
		else if (source == colorConfigPanel)
			doActionColorConfig();
		else if (source == radialS)
			doActionRadialOffset();
		else if (source == radialResetB)
			doActionRadialReset();
		else if (source == showSpacecraftCB)
			refTrackManager.setShowSourcePoints(showSpacecraftCB.isSelected());
		else if (source == errorModeBox)
			updateErrorUI();
		else if (source == showErrorCB)
			updateErrorUI();

		updateGui();
		updateErrorUI();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsChanged)
		{
			radialS.setModelValue(refTrackManager.getRadialOffset());
			configureColumnWidths();
		}
		else if (aEventType == ItemEventType.ItemsSelected)
		{
			List<LidarTrack> pickL = refTrackManager.getSelectedItems().asList();

			LidarTrack tmpTrack = null;
			if (pickL.size() > 0)
				tmpTrack = pickL.get(pickL.size() - 1);

			if (aSource != refTrackManager)
				lidarILP.scrollToItem(tmpTrack);
		}

		updateGui();
		updateErrorUI();
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if we are are not associated with the PickTarget
		if (LidarGuiUtil.isAssociatedPickTarget(aPrimaryTarg, refTrackManager) == false)
			return;

		// Bail if not a valid pick action
		if (PickUtil.isPopupTrigger(aEvent) == false || aMode != PickMode.ActiveSec)
			return;

		// Show the popup
		Component tmpComp = aEvent.getComponent();
		int posX = ((MouseEvent) aEvent).getX();
		int posY = ((MouseEvent) aEvent).getY();
		popupMenu.show(tmpComp, posX, posY);
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = lidarPicker == refPickManager.getActivePicker();
		dragB.setSelected(tmpBool);
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == pointSizeSpinner)
			doActionPointSize();
	}

	/**
	 * Helper method to configure the column widths on the track table
	 */
	private void configureColumnWidths()
	{
		int maxPts = 99;
		String sourceStr = "Data Source";
		for (LidarTrack aTrack : refTrackManager.getAllItems())
		{
			maxPts = Math.max(maxPts, aTrack.getNumberOfPoints());
			String tmpStr = TrackItemHandler.getSourceFileString(aTrack);
			if (tmpStr.length() > sourceStr.length())
				sourceStr = tmpStr;
		}

		JTable tmpTable = lidarILP.getTable();
		String trackStr = "" + tmpTable.getRowCount();
		String pointStr = "" + maxPts;
		String begTimeStr = "9999-88-88T00:00:00.000000";
		String endTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, blackCP, trackStr, pointStr, begTimeStr, endTimeStr, sourceStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}

	/**
	 * Helper method that handles the color config action
	 */
	private void doActionColorConfig()
	{
		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
		GroupColorProvider tgtGCP = colorConfigPanel.getTargetGroupColorProvider();
		refTrackManager.installGroupColorProviders(srcGCP, tgtGCP);
	}

	/**
	 * Helper method that handles the drag action
	 */
	private void doActionDrag()
	{
		Picker targPicker = null;
		if (dragB.isSelected() == true)
			targPicker = lidarPicker;

		refPickManager.setActivePicker(targPicker);
		if (targPicker == null)
			refTrackManager.setSelectedPoint(null, null);
	}

	/**
	 * Helper method that handles the pointSize action
	 */
	private void doActionPointSize()
	{
		Number val = (Number) pointSizeSpinner.getValue();
		refTrackManager.setPointSize(val.intValue());
	}

	/**
	 * Helper method that handles the radialOffset action
	 */
	private void doActionRadialOffset()
	{
		if (radialS.getValueIsAdjusting() == true)
			return;

		double tmpVal = radialS.getModelValue() * radialOffsetScale;
		refTrackManager.setRadialOffset(tmpVal);
	}

	/**
	 * Helper method that handles the radial reset action
	 */
	private void doActionRadialReset()
	{
		radialS.setModelValue(0.0);
		refTrackManager.setRadialOffset(0.0);
	}

	/**
	 * Helper method that handles the save action
	 */
	private void doActionSave()
	{
		if (saveDialog == null)
			saveDialog = new LidarSaveDialog(this, refTrackManager);

		saveDialog.setVisible(true);
	}

	/**
	 * Helper method that handles the drag action.
	 */
	private void doActionTranslate(List<LidarTrack> aTrackL)
	{
		if (translateDialog == null)
			translateDialog = new LidarTrackTranslateDialog(this, refTrackManager);

		translateDialog.setVisible(true);
	}

	/**
	 * Helper method that forms the configuration options that are placed on the
	 * left side.
	 */
	private JPanel formLeftPanel()
	{
		JPanel retPanel = new JPanel(new MigLayout("", "[]", "0[][]"));

		// Row 1: hideB, dragB, removeB
		retPanel.add(hideB, "sg g1,span,split");
		retPanel.add(dragB, "gapleft 10,sg g2");
		retPanel.add(removeB, "gapleft 10,sg g3,wrap");

		// Row 2: showB, translateB, saveB
		retPanel.add(showB, "sg g1,span,split");
		retPanel.add(translateB, "gapleft 10,sg g2");
		retPanel.add(saveB, "gapleft 10,sg g3,wrap");

		// Row 3: showSpacecraftCB
		retPanel.add(showSpacecraftCB, "span,wrap");

		// Radial offset section
		retPanel.add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");
		retPanel.add(new JLabel("Radial Offset", JLabel.LEFT), "growx,span,wrap 2");
		retPanel.add(radialS, "growx,pushx,span,split");
		retPanel.add(radialResetB, "wrap");

		return retPanel;
	}

	/**
	 * Helper method to update the error UI
	 */
	private void updateErrorUI()
	{
		boolean tmpBool = showErrorCB.isSelected();
		errorModeL.setEnabled(tmpBool);
		errorModeBox.setEnabled(tmpBool);

		// Bail if error computations are disabled
		String errorStr = "";
		if (tmpBool == false)
		{
			errorValueL.setText(errorStr);
			return;
		}

		// Calculate the error computations and update the relevant display
		ItemGroup errorMode = (ItemGroup) errorModeBox.getSelectedItem();
		Set<LidarTrack> selectedS = new HashSet<>(refTrackManager.getSelectedItems());

		// Calculate the cumulative track error and number of lidar points
		double errorSum = 0.0;
		int cntPoints = 0;
		int cntTracks = 0;
		for (LidarTrack aTrack : refTrackManager.getAllItems())
		{
			// Skip over Tracks that we are not interested in
			if (errorMode == ItemGroup.Visible && refTrackManager.getIsVisible(aTrack) == false)
				continue;
			if (errorMode == ItemGroup.Selected && selectedS.contains(aTrack) == false)
				continue;

			errorSum += refTrackManager.getTrackError(aTrack);
			cntPoints += aTrack.getNumberOfPoints();
			cntTracks++;
		}

		// Calculate RMS error
		double errorRMS = Math.sqrt(errorSum / cntPoints);
		if (cntTracks == 0 || cntPoints == 0)
			errorRMS = 0.0;

		// Update the errorValueL
		SigFigNumberFormat errFormat = new SigFigNumberFormat(7, "---");
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		errorStr = errFormat.format(errorRMS) + " RMS: ";
		errorStr += cntFormat.format(cntTracks) + " tracks ";
		errorStr += "(" + cntFormat.format(cntPoints) + " points)";
		errorValueL.setText(errorStr);
	}

	/**
	 * Updates the various UI elements to keep them synchronized
	 */
	private void updateGui()
	{
		// Update various buttons
		int cntFullItems = refTrackManager.getAllItems().size();
		boolean isEnabled = cntFullItems > 0;
		dragB.setEnabled(isEnabled);
		selectInvertB.setEnabled(isEnabled);

		int cntFullPoints = 0;
		for (LidarTrack aTrack : refTrackManager.getAllItems())
			cntFullPoints += aTrack.getNumberOfPoints();

		Set<LidarTrack> pickS = refTrackManager.getSelectedItems();
		int cntPickItems = pickS.size();
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0;
		removeB.setEnabled(isEnabled);
		translateB.setEnabled(isEnabled);
		saveB.setEnabled(isEnabled);

		int cntPickPoints = 0;
		int cntShowItems = 0;
		for (LidarTrack aTrack : pickS)
		{
			cntPickPoints += aTrack.getNumberOfPoints();
			if (refTrackManager.getIsVisible(aTrack) == true)
				cntShowItems++;
		}

		isEnabled = cntPickItems > 0 && cntShowItems < cntPickItems;
		showB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0 && cntShowItems > 0;
		hideB.setEnabled(isEnabled);

		isEnabled = refTrackManager.getRadialOffset() != 0;
		radialResetB.setEnabled(isEnabled);

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Tracks: " + cntFormat.format(cntFullItems);
		String helpStr = "Points: " + cntFormat.format(cntFullPoints);
		if (cntPickItems > 0)
		{
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";
			helpStr += "  (Selected: " + cntFormat.format(cntPickPoints) + ")";
		}
		titleL.setText(infoStr);
		titleL.setToolTipText(helpStr);
	}

}
