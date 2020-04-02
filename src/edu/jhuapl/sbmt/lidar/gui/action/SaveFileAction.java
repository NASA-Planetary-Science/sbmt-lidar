package edu.jhuapl.sbmt.lidar.gui.action;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.lidar.LidarFileSpecManager;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Save Tracks".
 *
 * @author lopeznr1
 */
class SaveFileAction extends PopAction<LidarFileSpec>
{
	// Ref vars
	private final LidarFileSpecManager refManager;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public SaveFileAction(LidarFileSpecManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<LidarFileSpec> aItemL)
	{
		Component rootComp = JOptionPane.getFrameForComponent(refParent);
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

	@Override
	public void setChosenItems(Collection<LidarFileSpec> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Update our associated MenuItem
		String displayStr = MessageUtil.toPluralForm("Save File", aItemC);
		aAssocMI.setText(displayStr);
	}

}
