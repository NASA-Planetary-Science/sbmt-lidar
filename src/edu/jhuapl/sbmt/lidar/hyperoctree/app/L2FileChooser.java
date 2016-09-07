package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class L2FileChooser extends PoliteFileChooser
{

    public L2FileChooser()
    {
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setMultiSelectionEnabled(true);
        setFileFilter(new FileNameExtensionFilter("OLA L2 Files", "l2"));
        setControlButtonsAreShown(false);
    }

}
