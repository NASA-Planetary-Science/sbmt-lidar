package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class PoliteFileChooser extends JFileChooser implements ActionListener, PropertyChangeListener
{
    Preferences preferences;
    final static String LAST_USED_FOLDER="lastUsedFolder";
    JButton upOneDirectoryButton=new JButton("▲");

    public PoliteFileChooser()
    {
        preferences=Preferences.userRoot().node(getClass().getName());
        String lastPath=preferences.get(LAST_USED_FOLDER, "none");
        if (lastPath=="none")
        {
            lastPath="/";
            preferences.put(LAST_USED_FOLDER, lastPath);
        }
        setCurrentDirectory(new File(lastPath));
        addPropertyChangeListener(this);
        // add up-one-directory button
        ((JPanel)getComponent(1)).setLayout(new FlowLayout());
        ((JPanel)getComponent(1)).add(upOneDirectoryButton);
        upOneDirectoryButton.addActionListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY))
            preferences.put(LAST_USED_FOLDER, getCurrentDirectory().getAbsolutePath());
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(upOneDirectoryButton))
        {
            File parentDirectory=getCurrentDirectory().getParentFile();
            if (parentDirectory!=null && parentDirectory.exists())
                setCurrentDirectory(parentDirectory);
        }
    }

}
