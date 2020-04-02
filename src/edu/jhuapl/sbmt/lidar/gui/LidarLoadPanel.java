package edu.jhuapl.sbmt.lidar.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.TrackFileType;
import edu.jhuapl.sbmt.lidar.util.LidarFileUtil;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used to provide a load (import) lidar tracks capability.
 */
public class LidarLoadPanel extends JPanel implements ActionListener
{
	// Ref vars
	private LidarTrackManager refManager;

	// GUI vars
	private JButton loadB;
	private JComboBox<String> fileTypeBox;

	public LidarLoadPanel(LidarTrackManager aManager)
	{
		refManager = aManager;

		setLayout(new MigLayout("", "", "[]0"));

		loadB = new JButton("Load Tracks");
		loadB.addActionListener(this);
		loadB.setToolTipText("Select lidar files to import.");
		JLabel tmpL = new JLabel("File Type:");
		fileTypeBox = new JComboBox<>();
		fileTypeBox.setToolTipText( //
				"<html>\nTrack file can be in either text or binary format.<br><br>\n" //
						+ "All ascii data are space delimited files (except for the Hayabusa2 lidar data).<br>\n" //
						+ "If you are importing a track exported from SBMT use Time, Lidar, S/C position ascii input.\n" //
						+ "Note that time is expressed either as a UTC string such as 2000-04-06T13:19:12.153<br>\n" //
						+ "or as a floating point ephemeris time such as 9565219.901.<br>\n<br>\n" //
						+ "If binary, each record must consist of 7 double precision values:<br>\n" //
						+ "1. ET<br>\n" //
						+ "2. X target<br>\n" //
						+ "3. Y target<br>\n" //
						+ "4. Z target<br>\n" //
						+ "5. X spacecraft position<br>\n" //
						+ "6. Y spacecraft position<br>\n" //
						+ "7. Z spacecraft position<br>\n");
		fileTypeBox.setModel(new DefaultComboBoxModel<>(TrackFileType.names()));
		add(tmpL, "span,split");
		add(fileTypeBox, "growx,w 0::");
		add(loadB, "");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == loadB)
			doLoadAction();
	}

	/**
	 * Helper method that handles the load action.
	 */
	private void doLoadAction()
	{
		// Bail if no file(s) specified
		File[] fileArr = CustomFileChooser.showOpenDialog(this, "Select Lidar Files", null, true);
		if (fileArr == null)
			return;
		List<File> tmpFileL = Arrays.asList(fileArr);
		tmpFileL.sort(null);

		// Load the file
		TrackFileType trackFileType = TrackFileType.find(fileTypeBox.getSelectedItem().toString());
		try
		{
			// Form a combined list of existing tracks and loaded tracks
			List<LidarTrack> tmpTrackL = new ArrayList<>();
			tmpTrackL.addAll(refManager.getAllItems());
			tmpTrackL.addAll(LidarFileUtil.loadLidarTracksFromFiles(refManager, fileArr, trackFileType));

			// Install the tracks
			refManager.setAllItems(tmpTrackL);
		}
		catch (IOException aExp)
		{
			String errMsg = "There was an error reading the file.";
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), errMsg, "Error",
					JOptionPane.ERROR_MESSAGE);

			aExp.printStackTrace();
		}

	}

}
