package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OlaFSHyperTreeGeneratorApp extends JFrame
{
    L2FileCollector collector=new L2FileCollector();

    public OlaFSHyperTreeGeneratorApp()
    {
        setJMenuBar(new OlaFSHyperTreeMenuBar(openL2FileSetAction,saveL2FileSetAction));
        openL2FileSetAction.putValue(AbstractAction.NAME, "Open .l2 file set");
        saveL2FileSetAction.putValue(AbstractAction.NAME, "Save as .l2 file set");
        add(collector);
    }

    AbstractAction openL2FileSetAction=new AbstractAction()
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser fc=new PoliteFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileFilter(new FileNameExtensionFilter("OLA L2 file sets", "l2s"));
            if (fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
            {
                L2FileSet fileSet=new L2FileSet();
                fileSet.load(fc.getSelectedFile().toPath());
                //
                collector.getFileListing().clear();
                for (int i=0; i<fileSet.getFiles().size(); i++)
                    collector.getFileListing().addFile(new File(fileSet.getFiles().get(i)));
            }

        }
    };

    AbstractAction saveL2FileSetAction=new AbstractAction()
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser fc=new PoliteFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileFilter(new FileNameExtensionFilter("OLA L2 file sets", "l2s"));
            //
            JLabel descriptionLabel=new JLabel("Optional Description: ");
            JTextField descriptionField=new JTextField();
            JPanel descriptionPanel=new JPanel();
            descriptionPanel.add(descriptionLabel);
            descriptionPanel.add(descriptionField);
            descriptionField.setPreferredSize(new Dimension((int)fc.getPreferredSize().getWidth()/3,(int)descriptionField.getPreferredSize().getHeight()));
            fc.add(descriptionPanel);
            //
            if (fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
            {
                L2FileSet fileSet=new L2FileSet();
                for (int i=0; i<collector.getFileListing().getListModel().size(); i++)
                    fileSet.addFile(collector.getFileListing().getListModel().getElementAt(i));
                fileSet.setDescription(descriptionField.getText());
                fileSet.save(fc.getSelectedFile().toPath());
            }
        }
    };

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                OlaFSHyperTreeGeneratorApp app=new OlaFSHyperTreeGeneratorApp();
                app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                app.setSize(1400,600);
                app.setVisible(true);
            }
        });
    }
}
