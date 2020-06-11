package edu.jhuapl.sbmt.lidar.gui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.util.LidarQueryUtil.DataType;
import edu.jhuapl.sbmt.model.image.Instrument;

import net.miginfocom.swing.MigLayout;

/**
 * UI component that holds 3 sub panels:
 * <UL>
 * <LI>Browse panel: Provides UI to browse fixed list of lidar data products
 * <LI>Custom panel: Provides UI to browse custom list of lidar data products
 * <LI>Search panel: Provides UI to allow user to query for list of lidar tracks
 * </UL>
 *
 * @author lopeznr1
 */
public class LidarPanel extends JTabbedPane
{
	/**
	 * Standard Constructor
	 */
	public LidarPanel(ModelManager aModelManager, PickManager aPickManager, Renderer aRenderer,
			SmallBodyViewConfig aBodyViewConfig)
	{
		// Custom panel
		JPanel customPanel = formCustomPanel(aModelManager, aPickManager, aRenderer);
		addTab("Custom", customPanel);

		// Bail if there is not (instrument) specific lidar data
		if (aBodyViewConfig.hasLidarData == false)
			return;

		// Browse panel
		JPanel browsePanel = formBrowsePanel(aModelManager, aPickManager, aRenderer, aBodyViewConfig);
		addTab("Browse", browsePanel);

		// Search panel
		JPanel searchPanel = formSearchPanel(aModelManager, aPickManager, aRenderer, aBodyViewConfig);
		if (searchPanel != null)
			addTab("Search", searchPanel);

		// Default selected panel should be the browse panel
		setSelectedComponent(browsePanel);
	}

	/**
	 * Helper utility method that forms the 'browse' tabbed panel.
	 */
	private static JPanel formBrowsePanel(SceneChangeNotifier aSceneChangeNotifier, PickManager aPickManager,
			Renderer aRenderer, SmallBodyViewConfig aBodyViewConfig)
	{
		// Determine the proper data source name
		Instrument tmpInstrument = aBodyViewConfig.lidarInstrumentName;
		String dataSourceName = null;
		if (tmpInstrument == Instrument.LASER)
			dataSourceName = "Hayabusa2";
		else if (tmpInstrument == Instrument.OLA)
			dataSourceName = "Default";

		// Form the LidarFileSpecManager
		LidarFileSpecManager tmpFileSpecManager = new LidarFileSpecManager(aSceneChangeNotifier, aBodyViewConfig);
		aRenderer.addVtkPropProvider(tmpFileSpecManager);

		// Manually register for events of interest
		aPickManager.getDefaultPicker().addListener(tmpFileSpecManager);
		aPickManager.getDefaultPicker().addPropProvider(tmpFileSpecManager);

		// Form the "browse" panel
		LidarFileSpecPanel retPanel = new LidarFileSpecPanel(tmpFileSpecManager, aPickManager, aRenderer, aBodyViewConfig,
				dataSourceName);
		return retPanel;
	}

	/**
	 * Helper utility method that forms the 'custom' tabbed panel.
	 */
	private static JPanel formCustomPanel(ModelManager aModelManager, PickManager aPickManager, Renderer aRenderer)
	{
		// Form the LidarTrackManager
		PolyhedralModel tmpSmallBody = aModelManager.getPolyhedralModel();
		LidarTrackManager tmpTrackManager = new LidarTrackManager(aModelManager, tmpSmallBody);
		aRenderer.addVtkPropProvider(tmpTrackManager);

		// Manually register for events of interest
		aPickManager.getDefaultPicker().addListener(tmpTrackManager);
		aPickManager.getDefaultPicker().addPropProvider(tmpTrackManager);

		// Form the 'custom' panel
		LidarLoadPanel tmpLidarLoadPanel = new LidarLoadPanel(tmpTrackManager);
		LidarTrackPanel tmpLidarListPanel = new LidarTrackPanel(tmpTrackManager, aPickManager, aRenderer, aModelManager);

		JPanel retPanel = new JPanel(new MigLayout("", "", "0[]0"));
		retPanel.add(tmpLidarLoadPanel, "growx,wrap");
		retPanel.add(tmpLidarListPanel, "growx,growy,pushx,pushy");
		return retPanel;
	}

	/**
	 * Helper utility method that forms the 'search' tabbed panel.
	 */
	private static JPanel formSearchPanel(ModelManager aModelManager, PickManager aPickManager, Renderer aRenderer,
			SmallBodyViewConfig aBodyViewConfig)
	{
		// MOLA search isn't working, so disable it for now.
		if (aBodyViewConfig.lidarInstrumentName.equals(Instrument.MOLA) == true)
			return null;

		boolean hasHyperTreeSearch = aBodyViewConfig.hasHypertreeBasedLidarSearch;
		Instrument tmpInstrument = aBodyViewConfig.lidarInstrumentName;

		// Form the LidarTrackManager
		PolyhedralModel tmpSmallBody = aModelManager.getPolyhedralModel();
		LidarTrackManager tmpTrackManager = new LidarTrackManager(aModelManager, tmpSmallBody);
		aRenderer.addVtkPropProvider(tmpTrackManager);

		// Manually register for events of interest
		aPickManager.getDefaultPicker().addListener(tmpTrackManager);
		aPickManager.getDefaultPicker().addPropProvider(tmpTrackManager);

		// Form the appropriate 'search' panel
		DataType dataType = null;
		if (tmpInstrument == Instrument.MOLA && hasHyperTreeSearch == true)
			dataType = DataType.Mola;
		else if (tmpInstrument == Instrument.LASER && hasHyperTreeSearch == true)
			dataType = DataType.Hayabusa;
		else if (tmpInstrument == Instrument.OLA && hasHyperTreeSearch == true)
			dataType = DataType.Ola;

		LidarSearchPanel searchPanel;
		if (dataType == null)
			searchPanel = new LidarSearchPanel(aBodyViewConfig, aModelManager, aPickManager, tmpTrackManager);
		else
			searchPanel = new LidarHyperTreeSearchPanel(aBodyViewConfig, aModelManager, aPickManager, tmpTrackManager,
					dataType);

		// Form the 'list' panel
		JPanel trackPanel = new LidarTrackPanel(tmpTrackManager, aPickManager, aRenderer, aModelManager);

		JPanel retPanel = new JPanel(new MigLayout("", "0[]0", "0[]0"));
		retPanel.add(searchPanel, "growx,span,wrap");
		retPanel.add(trackPanel, "growx,growy,pushx,pushy");
		return retPanel;
	}
}
