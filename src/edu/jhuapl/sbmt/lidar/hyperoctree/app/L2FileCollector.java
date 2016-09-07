package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;

public class L2FileCollector extends JPanel implements ActionListener
{

    L2FileChooser chooser=new L2FileChooser();
    FileListPane fileListing=new FileListPane();
    JPanel controlPanel=new JPanel();
    JButton addButton=new JButton("⇒");
    JButton remButton=new JButton("×");
    JPanel rightPanel=new JPanel();

    public L2FileCollector()
    {
        fileListing.scrollPane.setPreferredSize(chooser.getPreferredSize());

        addButton.setSize(new Dimension(50,50));
        remButton.setSize(new Dimension(50,50));
        addButton.addActionListener(this);
        remButton.addActionListener(this);

        controlPanel.setLayout(new GridLayout(2, 1));
        controlPanel.add(addButton);
        controlPanel.add(remButton);

        rightPanel.setLayout(new FlowLayout());
        rightPanel.add(controlPanel);
        rightPanel.add(fileListing);

        setLayout(new FlowLayout());
        add(chooser);
        add(rightPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(addButton))
        {
            File[] files=chooser.getSelectedFiles();
            for (int f=0; f<files.length; f++)
            {
                fileListing.addFile(files[f]);
            }
        }
        if (e.getSource().equals(remButton))
        {
            fileListing.removeSelectedFiles();
        }
    }

}
