package edu.jhuapl.sbmt.lidar.gui;

import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.dem.Dem;
import edu.jhuapl.sbmt.dem.vtk.VtkDemPainter;
import edu.jhuapl.sbmt.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.lidar.LidarManager;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.lidar.util.LidarQueryUtil.DataType;

import net.miginfocom.swing.MigLayout;

/**
 * UI component that holds 3 sub panels:
 * <ul>
 * <li>Browse panel: Provides UI to browse fixed list of lidar data products
 * <li>Custom panel: Provides UI to browse custom list of lidar data products
 * <li>Search panel: Provides UI to allow user to query for list of lidar tracks
 * </ul>
 *
 * @author lopeznr1
 */
public class LidarPanel extends JTabbedPane
{
	/** Standard Constructor */
	public LidarPanel(Renderer aRenderer, StatusNotifier aStatusNotifier, PickManager aPickManager,
			LidarInstrumentConfig config, PolyhedralModel aSmallBody, ModelManager aModelManager)
	{
		// Custom panel
		var customPanel = formCustomPanel(aRenderer, aStatusNotifier, aPickManager, aSmallBody, config);
		addTab("Custom", customPanel);

		// Bail if there is not (instrument) specific lidar data
		if (config.hasLidarData == false)
			return;

		// Browse panel
		var browsePanel = formBrowsePanel(aRenderer, aStatusNotifier, aPickManager, aSmallBody, config);
		addTab("Browse", browsePanel);

		// Search panel
		var searchPanel = formSearchPanel(aRenderer, aStatusNotifier, aPickManager, aSmallBody, config,
				aModelManager);
		if (searchPanel != null)
			addTab("Search", searchPanel);

		// Default selected panel should be the browse panel
		setSelectedComponent(browsePanel);
	}

	/**
	 * Helper utility method that forms the 'browse' tabbed panel.
	 */
	private static JPanel formBrowsePanel(Renderer aRenderer, StatusNotifier aStatusNotifier, PickManager aPickManager,
			PolyhedralModel aSmallBody, LidarInstrumentConfig config)
	{
		// Determine the proper data source name
		Instrument tmpInstrument = config.lidarInstrumentName;
		String dataSourceName = null;
		if (tmpInstrument == Instrument.LASER)
			dataSourceName = "Hayabusa2";
		else if (tmpInstrument == Instrument.OLA)
			dataSourceName = "Default";

		// Form the LidarFileSpecManager
		var tmpItemManager = new LidarFileSpecManager(aRenderer, aStatusNotifier, config);
		aRenderer.addVtkPropProvider(tmpItemManager);

		// Manually register for events of interest
		aPickManager.getDefaultPicker().addListener(tmpItemManager);
		aPickManager.getDefaultPicker().addPropProvider(tmpItemManager);
		tmpItemManager.addLoadListener((aSource, aItemC) -> handleLoadChange(tmpItemManager, aItemC, aPickManager));

		// Form the "browse" panel
		var retPanel = new LidarFileSpecPanel(tmpItemManager, aPickManager, aRenderer, config, dataSourceName);
		return retPanel;
	}

	/**
	 * Helper utility method that forms the 'custom' tabbed panel.
	 */
	private static JPanel formCustomPanel(Renderer aRenderer, StatusNotifier aStatusNotifier, PickManager aPickManager,
			PolyhedralModel aSmallBody, LidarInstrumentConfig config)
	{
		// Form the LidarTrackManager
		var tmpItemManager = new LidarTrackManager(aRenderer, aStatusNotifier, aSmallBody);
		aRenderer.addVtkPropProvider(tmpItemManager);

		// Manually register for events of interest
		aPickManager.getDefaultPicker().addListener(tmpItemManager);
		aPickManager.getDefaultPicker().addPropProvider(tmpItemManager);
		tmpItemManager.addLoadListener((aSource, aItemC) -> handleLoadChange(tmpItemManager, aItemC, aPickManager));

		// Form the "custom" panel
		LidarLoadPanel tmpLidarLoadPanel = new LidarLoadPanel(tmpItemManager);
		LidarTrackPanel tmpLidarListPanel = new LidarTrackPanel(tmpItemManager, aRenderer, aPickManager, aSmallBody, config);

		var retPanel = new JPanel(new MigLayout("", "", "0[]0"));
		retPanel.add(tmpLidarLoadPanel, "growx,wrap");
		retPanel.add(tmpLidarListPanel, "growx,growy,pushx,pushy");
		return retPanel;
	}

	/**
	 * Helper utility method that forms the 'search' tabbed panel.
	 */
	private static JPanel formSearchPanel(Renderer aRenderer, StatusNotifier aStatusNotifier, PickManager aPickManager,
			PolyhedralModel aSmallBody, LidarInstrumentConfig config, ModelManager aModelManager)
	{
		// MOLA search isn't working, so disable it for now.
		if (config.lidarInstrumentName.equals(Instrument.MOLA) == true)
			return null;

		boolean hasHyperTreeSearch = config.hasHypertreeBasedLidarSearch;
		Instrument tmpInstrument = config.lidarInstrumentName;

		// Form the LidarTrackManager
		var tmpItemManager = new LidarTrackManager(aRenderer, aStatusNotifier, aSmallBody);
		aRenderer.addVtkPropProvider(tmpItemManager);

		// Manually register for events of interest
		aPickManager.getDefaultPicker().addListener(tmpItemManager);
		aPickManager.getDefaultPicker().addPropProvider(tmpItemManager);
		tmpItemManager.addLoadListener((aSource, aItemC) -> handleLoadChange(tmpItemManager, aItemC, aPickManager));

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
			searchPanel = new LidarSearchPanel(config, aModelManager, aPickManager, tmpItemManager);
		else
			searchPanel = new LidarHyperTreeSearchPanel(config, aModelManager, aPickManager, tmpItemManager,
					dataType);

		// Form the "list" panel
		var trackPanel = new LidarTrackPanel(tmpItemManager, aRenderer, aPickManager, aSmallBody, config);

		var retPanel = new JPanel(new MigLayout("", "0[]0", "0[]0"));
		retPanel.add(searchPanel, "growx,span,wrap");
		retPanel.add(trackPanel, "growx,growy,pushx,pushy");
		return retPanel;
	}

	/**
	 * Utility helper method that notifies the {@link PickManager}'s default
	 * picker that a {@link VtkPropProvider}'s state has changed.
	 * <p>
	 * This notification will be sent only if {@link VtkDemPainter} corresponding
	 * to the specified {@link Dem} has reached a "ready" state.
	 */
	private static <G1> void handleLoadChange(LidarManager<G1> aItemManager, Collection<G1> aItemC,
			PickManager aPickManager)
	{
		aPickManager.getDefaultPicker().notifyPropProviderChanged();
	}

}
