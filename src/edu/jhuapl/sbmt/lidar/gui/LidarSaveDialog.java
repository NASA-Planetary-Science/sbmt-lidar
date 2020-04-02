package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.util.LidarFileUtil;

import glum.gui.GuiUtil;
import glum.gui.component.GTextField;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import net.miginfocom.swing.MigLayout;

/**
 * Save dialog that provides a custom UI for fine grained control when saving
 * LidarTracks.
 *
 * @author lopeznr1
 */
public class LidarSaveDialog extends JDialog implements ActionListener, ItemEventListener
{
	// Reference vars
	private final LidarTrackManager refManager;

	// GUI vars
	private JLabel infoL, warnL;
	private JButton acceptB;
	private JButton cancelB;
	private JTextField folderTF;
	private JButton folderB;
	private GTextField baseNameTF;
	private JComboBox<ModeType> modeBox;
	private JCheckBox rotaCB;

	/**
	 * Standard Constructor
	 */
	public LidarSaveDialog(Component aParent, LidarTrackManager aManager)
	{
		super(JOptionPane.getFrameForComponent(aParent));

		refManager = aManager;

		formGui();
		pack();
		setLocationRelativeTo(aParent);

		// Register for events of interest
		refManager.addListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == acceptB)
			doActionAccept();
		if (source == cancelB)
			setVisible(false);
		if (source == folderB)
			doActionPickFolder();

		updateGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		// Ignore events while we are not displayed
		if (isVisible() == false)
			return;

		updateGui();
	}

	@Override
	public void setVisible(boolean aBool)
	{
		// Update our internal UI if we are not currently visible
		if (isVisible() == false)
			updateGui();

		super.setVisible(aBool);
	}

	/**
	 * Helper method that handles the apply action.
	 */
	private void doActionAccept()
	{
		Set<LidarTrack> trackS = refManager.getSelectedItems();
		File folder = new File(folderTF.getText());
		String baseName = baseNameTF.getText();
		boolean isRota = rotaCB.isSelected();

		// Save the Tracks to a unified file
		if (modeBox.getSelectedItem() == ModeType.Single)
		{
			File unifiedFile = getUnifiedFile();
			try
			{
				LidarFileUtil.saveTracksToTextFile(refManager, unifiedFile, trackS, isRota);
			}
			catch (IOException aExp)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
						"Unable to save file to " + unifiedFile, "Error Saving File", JOptionPane.ERROR_MESSAGE);

				aExp.printStackTrace();
			}
		}
		else
		{
			try
			{
				LidarFileUtil.saveTracksToFolder(refManager, folder, trackS, baseName, isRota);
			}
			catch (IOException aExp)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
						"Unable to save tracks to folder " + folder, "Error Saving File", JOptionPane.ERROR_MESSAGE);

				aExp.printStackTrace();
			}
		}

		setVisible(false);
	}

	/**
	 * Helper method that handles the pick folder action.
	 */
	private void doActionPickFolder()
	{
		// Bail if no file selected
		File tmpFile = DirectoryChooser.showOpenDialog(this);
		if (tmpFile == null)
			return;

		// Update folderTF
		String tmpStr = tmpFile.getAbsolutePath();
		folderTF.setText(tmpStr);

		updateGui();
	}

	/**
	 * Helper method that forms the GUI.
	 */
	private void formGui()
	{
		setTitle("Save Tracks");
		setLayout(new MigLayout());

		// Info area
		infoL = new JLabel("Selected Tracks: 0");
		add(infoL, "span,wrap");

		// Folder area
		JLabel folderL = new JLabel("Folder:");
		folderTF = new GTextField(this, "");
		folderB = GuiUtil.formButton(this, "...");
		add(folderL, "right");
		add(folderTF, "growx,pushx");
		add(folderB, "wrap");

		// Base filename area
		JLabel baseNameL = new JLabel("Basename:");
		baseNameTF = new GTextField(this, "track");
		add(baseNameL, "right");
		add(baseNameTF, "growx,span,w :400:,wrap");

		// Mode area
		JLabel modeL = new JLabel("Mode:");
		modeBox = new JComboBox<>(ModeType.values());
		modeBox.addActionListener(this);
		add(modeL, "right");
		add(modeBox, "wrap");

		// Radial offset / Translation area
		rotaCB = new JCheckBox("Radial Offset and Traslation Applied");
		add(rotaCB, "span,wrap");

		// Warn area
		warnL = new JLabel("Changes have not been applied.");
		warnL.setPreferredSize(warnL.getPreferredSize());
		add(warnL, "growx,w 0:0:,span,wrap");

		// Action area: Accept, Cancel
		acceptB = GuiUtil.formButton(this, "Accept");
		cancelB = GuiUtil.formButton(this, "Cancel");
		add(cancelB, "span,split,align right");
		add(acceptB);
	}

	/**
	 * Helper method that defines the file for which multiple tracks should be
	 * saved to.
	 */
	private File getUnifiedFile()
	{
		String folderName = folderTF.getText();
		String baseName = baseNameTF.getText();

		File retFile = Paths.get(folderName, baseName + ".tab").toFile();
		return retFile;
	}

	/**
	 * Helper method to keep the GUI synchronized
	 */
	private void updateGui()
	{
		Set<LidarTrack> trackS = refManager.getSelectedItems();

		boolean isValidFolder = folderTF.getText().length() > 0;
		boolean isValidBaseName = baseNameTF.getText().trim().length() > 0;

		// Determine the unified file
		File unifiedFile = getUnifiedFile();

		// Determine the number of files that will be overwritten
		File folder = null;
		if (folderTF.getText().length() > 0)
			folder = new File(folderTF.getText());
		String baseName = baseNameTF.getText();
		int cntFilesOverwrite = 0;
		for (int aIdx = 0; aIdx < trackS.size(); aIdx++)
		{
			File tmpFile = LidarFileUtil.getFileForIndex(folder, baseName, aIdx);
			if (tmpFile != null && tmpFile.exists() == true)
				cntFilesOverwrite++;
		}

		// Update the infoL / warnL
		infoL.setText("Selected Tracks: " + trackS.size());

		String regMsg = "";
		String errMsg = null;
		if (trackS.size() == 0)
			errMsg = "There are no selected tracks.";
		else if (isValidFolder == false)
			errMsg = "Please enter valid folder.";
		else if (folder.exists() == false)
			errMsg = "The specified folder does not exist.";
		else if (folder.isDirectory() == false)
			errMsg = "The specified folder is not a directory.";
		else if (Files.isWritable(folder.toPath()) == false)
			errMsg = "The specified folder is not a writable.";
		else if (isValidBaseName == false)
			errMsg = "Please enter valid base name.";
		else if (modeBox.getSelectedItem() == ModeType.Single && unifiedFile.exists() == true)
			regMsg = "File, " + unifiedFile.getName() + ", alread exists. It will be overwritten.";
		else if (modeBox.getSelectedItem() == ModeType.Single && unifiedFile.exists() == false)
			regMsg = "Tracks will be saved to: " + unifiedFile;
		else if (modeBox.getSelectedItem() == ModeType.Multiple && cntFilesOverwrite > 0)
			regMsg = "" + cntFilesOverwrite + " files will be overwritten in path: " + folder;
		else if (modeBox.getSelectedItem() == ModeType.Multiple && cntFilesOverwrite == 0)
			regMsg = "" + trackS.size() + " files will be saved to path: " + folder;

		warnL.setText(regMsg);
		if (errMsg != null)
			warnL.setText(errMsg);

		Color fgColor = Colors.getPassFG();
		if (errMsg != null)
			fgColor = Colors.getFailFG();
		warnL.setForeground(fgColor);

		// Update the action buttons
		boolean tmpBool = errMsg == null;
		acceptB.setEnabled(tmpBool);
	}

}

/**
 * Enum that defines the items to be selected.
 *
 * @author lopeznr1
 */
enum ModeType
{
	/** Enum that declares all tracks should be saved to the same file. */
	Single,

	/** Enum that declares each track should be saved to an individual file. */
	Multiple;

	@Override
	public String toString()
	{
		if (this == Single)
			return "Unified File";
		else
			return "Individual Files";
	}

};
