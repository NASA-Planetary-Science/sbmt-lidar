package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class FileListPane extends JPanel
{

    JScrollPane scrollPane;
    JList<String> list=new JList<String>();
    DefaultListModel<String> listModel=new DefaultListModel<String>();

    public FileListPane()
    {
        scrollPane=new JScrollPane(list);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setModel(listModel);
        add(scrollPane);
    }

    public void addFile(File file)
    {
        if (!listModel.contains(file.toString()))
            listModel.addElement(file.toString());
    }

    public void removeFile(File file)
    {
        listModel.removeElement(file.toString());
    }

    public void removeSelectedFiles()
    {
        int[] idx=list.getSelectedIndices();
        for (int i=0; i<idx.length; i++)
            listModel.remove(idx[i]);
    }

    public void clear()
    {
        listModel.clear();
    }

}
