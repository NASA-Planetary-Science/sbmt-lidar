package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class OlaFSHyperTreeMenuBar extends JMenuBar
{

    public OlaFSHyperTreeMenuBar(AbstractAction openL2FileSetAction, AbstractAction saveL2FileSetAction)
    {
        JMenu fileMenu=new JMenu("File");
        JMenuItem openL2FileSet=new JMenuItem();
        JMenuItem saveL2FileSet=new JMenuItem();
        openL2FileSet.setAction(openL2FileSetAction);
        saveL2FileSet.setAction(saveL2FileSetAction);

        fileMenu.add(openL2FileSet);
        fileMenu.add(saveL2FileSet);

        add(fileMenu);
    }

}
