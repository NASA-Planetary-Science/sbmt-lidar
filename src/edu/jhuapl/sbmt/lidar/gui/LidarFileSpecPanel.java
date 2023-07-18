package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.gui.ColorMode;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import edu.jhuapl.sbmt.core.gui.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.lidar.gui.action.LidarGuiUtil;
import edu.jhuapl.sbmt.lidar.gui.color.ColorConfigPanel;
import edu.jhuapl.sbmt.lidar.util.LidarBrowseUtil;
import edu.jhuapl.sbmt.lidar.util.LidarFileSpecLoadUtil;

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
import glum.gui.table.TablePopupHandler;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of lidar FileSpecs.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Display list of FileSpecs in a table
 * <LI>Allow user to show or hide associated tracks
 * <LI>Allow user to save files corresponding to FileSpecs
 * <LI>Allow user to show corresponding spacecraft positions (for all
 * LidarPoints)
 * </UL>
 *
 * @author lopeznr1
 */
public class LidarFileSpecPanel extends JPanel implements ActionListener, ItemEventListener, PickListener
{
	// Ref vars
	private LidarFileSpecManager refManager;

	// State vars
	private double radialOffsetScale;
	private String refDataSourceName;
	private String failFetchMsg;

	// GUI vars
	private final PopupMenu<?> popupMenu;
	private ItemListPanel<LidarFileSpec> lidarILP;
	private JLabel titleL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton hideB, showB;
	private JButton saveB;
	private ColorConfigPanel<?> colorConfigPanel;
	private PercentIntervalChanger timeIntervalChanger;
	private GSlider radialS;
	private JButton radialResetB;
	private JCheckBox showSpacecraftCB;

	/**
	 * Standard Constructor
	 */
	public LidarFileSpecPanel(LidarFileSpecManager aLidarManager, PickManager aPickManager, Renderer aRenderer,
			LidarInstrumentConfig aBodyViewConfig, String aDataSourceName)
	{
		refManager = aLidarManager;

		// TODO: Note the custom radial offset logic for Hayabusa
		radialOffsetScale = aBodyViewConfig.lidarOffsetScale;
		if (LidarFileSpecLoadUtil.isHayabusaData(aBodyViewConfig) == true)
			radialOffsetScale *= 10;

		refDataSourceName = aDataSourceName;
		failFetchMsg = null;

		setLayout(new MigLayout());

		// Popup menu
		popupMenu = LidarGuiUtil.formLidarFileSpecPopupMenu(refManager, this);

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

		// Table content
		boolean isShortMode = refDataSourceName == null;
		String timeMaxStr = "2019-08-08T08:08:08.080";
		if (isShortMode == false)
			timeMaxStr = "2019-08-08T08:08:08.080808";

		QueryComposer<LookUp> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(LookUp.IsVisible, Boolean.class, "Show", 50);
		tmpComposer.addAttribute(LookUp.Color, ColorProvider.class, "Color", 50);
		tmpComposer.addAttribute(LookUp.NumPoints, Integer.class, "# pts", "987,");
		tmpComposer.addAttribute(LookUp.Name, String.class, "Name", null);
		tmpComposer.addAttribute(LookUp.BegTime, Double.class, "Start Time", timeMaxStr);
		tmpComposer.addAttribute(LookUp.EndTime, Double.class, "End Time", timeMaxStr);
		tmpComposer.getItem(LookUp.NumPoints).maxSize *= 3;
		tmpComposer.getItem(LookUp.Name).defaultSize *= 3;

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(isShortMode);
		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setEditor(LookUp.Color, new ColorProviderCellEditor<>());
		tmpComposer.setRenderer(LookUp.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setRenderer(LookUp.NumPoints, new NumberRenderer("###,###,###", "---"));
		tmpComposer.setRenderer(LookUp.BegTime, tmpTimeRenderer);
		tmpComposer.setRenderer(LookUp.EndTime, tmpTimeRenderer);

		ItemHandler<LidarFileSpec> tmpIH = new FileSpecItemHandler(refManager, tmpComposer);
		ItemProcessor<LidarFileSpec> tmpIP = refManager;
		lidarILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		lidarILP.setSortingEnabled(true);

		JTable lidarTable = lidarILP.getTable();
		lidarTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		lidarTable.addMouseListener(new TablePopupHandler(refManager, popupMenu));
		add(new JScrollPane(lidarTable), "growx,growy,pushy,span,wrap");

		// Action buttons: hide / save / show
		hideB = GuiUtil.formButton(this, "Hide");
		hideB.setToolTipText("Hide Files");
		showB = GuiUtil.formButton(this, "Show");
		showB.setToolTipText("Show Files");
		saveB = GuiUtil.formButton(this, "Save");
		saveB.setToolTipText("Save Files");

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
		showSpacecraftCB.setSelected(true);
		showSpacecraftCB.addActionListener(this);

		// Form the left, right and bottom sides
		JPanel leftPanel = formLeftPanel();
		add(leftPanel, "growx,growy,pushx");

		add(GuiUtil.createDivider(), "growy,w 4!");

		colorConfigPanel = new ColorConfigPanel<>(this, refManager, aRenderer);
		colorConfigPanel.setActiveMode(ColorMode.Simple);
		add(colorConfigPanel, "ax right,ay top,growx,wrap");

		// Displayed lidar time interval
		timeIntervalChanger = new PercentIntervalChanger("Displayed Lidar Data");
		timeIntervalChanger.addActionListener(this);
		add(timeIntervalChanger, "growx,spanx,wrap 0");

		// Populate the table with the initialize data source
		populate(aBodyViewConfig, aDataSourceName);

		updateGui();

		// Register for events of interest
		refManager.addListener(this);
		aPickManager.getDefaultPicker().addListener(this);
	}

	/**
	 * Method that will populate the table with LidarFileSpecs relative to the
	 * specified data source.
	 */
	public void populate(LidarInstrumentConfig aBodyViewConfig, String aDataSourceName)
	{
		String browseFileList = null;
		if (aBodyViewConfig.lidarBrowseWithPointsDataSourceMap.get(aDataSourceName) != null)
		{
			browseFileList = aBodyViewConfig.lidarBrowseWithPointsDataSourceMap.get(aDataSourceName);
		}
		else
			browseFileList = aBodyViewConfig.lidarBrowseDataSourceMap.get(aDataSourceName);

		try
		{
//			FileCache.isFileGettable(aBodyViewConfig.lidarBrowseFileListResourcePath);

			List<LidarFileSpec> tmpL;
			if (aDataSourceName == null)
				tmpL = LidarBrowseUtil.loadLidarFileSpecListFor(aBodyViewConfig);
			else
				tmpL = LidarBrowseUtil.loadCatalog(browseFileList);

			refManager.setAllItems(tmpL);

			failFetchMsg = null;
		}
		catch (IOException aExp)
		{
			failFetchMsg = "Failure: " + aExp.getMessage();
			aExp.printStackTrace();
		}
		catch (UnauthorizedAccessException aExp)
		{
			failFetchMsg = "No Results Available: Access Not Authorized";
		}
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		List<LidarFileSpec> tmpL = refManager.getSelectedItems().asList();
		if (source == selectAllB)
			ItemManagerUtil.selectAll(refManager);
		else if (source == selectNoneB)
			ItemManagerUtil.selectNone(refManager);
		else if (source == selectInvertB)
			ItemManagerUtil.selectInvert(refManager);
		else if (source == hideB)
			refManager.setIsVisible(tmpL, false);
		else if (source == showB)
			refManager.setIsVisible(tmpL, true);
		else if (source == saveB)
			doActionSave();
		else if (source == colorConfigPanel)
			doActionColorConfig();
		else if (source == radialS)
			doActionRadialOffset();
		else if (source == radialResetB)
			doActionRadialReset();
		else if (source == timeIntervalChanger)
			refManager.setPercentageShown(timeIntervalChanger.getLowValue(), timeIntervalChanger.getHighValue());
		else if (source == showSpacecraftCB)
			refManager.setShowSourcePoints(showSpacecraftCB.isSelected());

		updateGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		updateGui();
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if we are are not associated with the PickTarget
		if (LidarGuiUtil.isAssociatedPickTarget(aPrimaryTarg, refManager) == false)
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

	/**
	 * Helper method that handles the color config action
	 */
	private void doActionColorConfig()
	{
		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
		GroupColorProvider tgtGCP = colorConfigPanel.getTargetGroupColorProvider();
		refManager.installGroupColorProviders(srcGCP, tgtGCP);
	}

	/**
	 * Helper method that handles the radialOffset action
	 */
	private void doActionRadialOffset()
	{
		if (radialS.getValueIsAdjusting() == true)
			return;

		double tmpVal = radialS.getModelValue() * radialOffsetScale;
		refManager.setRadialOffset(tmpVal);
	}

	/**
	 * Helper method that handles the radial reset action
	 */
	private void doActionRadialReset()
	{
		radialS.setModelValue(0.0);
		refManager.setRadialOffset(0.0);
	}

	/**
	 * Helper method that handles the save action.
	 */
	private void doActionSave()
	{
		Component rootComp = JOptionPane.getFrameForComponent(this);
		Set<LidarFileSpec> workS = refManager.getSelectedItems();

		// Prompt the user for the save folder
		String title = "Specify the folder to save " + workS.size() + " lidar files";
		File targPath = DirectoryChooser.showOpenDialog(rootComp, title);
		if (targPath == null)
			return;

		// Save all of the selected items into the target folder
		LidarFileSpec workFileSpec = null;
		int passCnt = 0;
		try
		{
			for (LidarFileSpec aFileSpec : workS)
			{
				workFileSpec = aFileSpec;
				File srcFile = FileCache.getFileFromServer(aFileSpec.getPath());
				File dstFile = new File(targPath, srcFile.getName());
				FileUtil.copyFile(srcFile, dstFile);
				passCnt++;
			}
		}
		catch (Exception aExp)
		{
			String errMsg = "Failed to save " + (workS.size() - passCnt) + "files. Failed on lidar file: ";
			errMsg += workFileSpec.getName();
			JOptionPane.showMessageDialog(rootComp, errMsg, "Error Saving Lidar Files", JOptionPane.ERROR_MESSAGE);
			aExp.printStackTrace();
		}
	}

	/**
	 * Helper method that forms the configuration options that are placed on the
	 * left side.
	 */
	private JPanel formLeftPanel()
	{
		JPanel retPanel = new JPanel(new MigLayout("", "[]", "0[][]"));

		// Row 1: hideB
		retPanel.add(hideB, "sg g1,wrap");

		// Row 2: showB, saveB
		retPanel.add(showB, "sg g1,span,split");
		retPanel.add(saveB, "gapleft 10,sg g2,wrap");

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
	 * Updates the various UI elements to keep them synchronized
	 */
	private void updateGui()
	{
		// Update various buttons
		int cntFullItems = refManager.getAllItems().size();
		boolean isEnabled = cntFullItems > 0;
		selectInvertB.setEnabled(isEnabled);

		Set<LidarFileSpec> pickS = refManager.getSelectedItems();
		int cntPickItems = pickS.size();
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0;
		saveB.setEnabled(isEnabled);

		int cntFullPts = 0;
		for (LidarFileSpec aFileSpec : refManager.getAllItems())
			cntFullPts += refManager.getNumberOfPoints(aFileSpec);

		int cntShowItems = 0;
		int cntPickPoints = 0;
		for (LidarFileSpec aItem : pickS)
		{
			if (refManager.getIsVisible(aItem) == true)
				cntShowItems++;
			if (refManager.isLoaded(aItem) == true)
				cntPickPoints += refManager.getNumberOfPoints(aItem);
		}

		isEnabled = cntPickItems > 0 && cntShowItems < cntPickItems;
		showB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0 && cntShowItems > 0;
		hideB.setEnabled(isEnabled);

		isEnabled = refManager.getRadialOffset() != 0;
		radialResetB.setEnabled(isEnabled);

		String extraTag = ": ";
		if (refDataSourceName != null)
			extraTag = " (" + refDataSourceName + ") : ";

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Files" + extraTag + cntFormat.format(cntFullItems);
		String helpStr = "Points " + cntFormat.format(cntFullPts);
		if (cntPickItems > 0)
		{
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";
			helpStr += "  (Selected: " + cntFormat.format(cntPickPoints) + ")";
		}
		if (failFetchMsg != null)
		{
			infoStr = failFetchMsg;
			helpStr = null;
		}
		titleL.setText(infoStr);
		titleL.setToolTipText(helpStr);
	}

}
