package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.gui.lidar.v2.LidarSearchView;

public class Hayabusa2LidarSearchView extends LidarSearchView
{

    JPanel scRangePanel = new JPanel();
    protected JFormattedTextField minSCRange;
    protected JFormattedTextField maxSCRange;

    public Hayabusa2LidarSearchView() {
        super();

        JLabel lblSpacecraftRange = new JLabel("Spacecraft Range:");
        GridBagConstraints gbc_lblSpacecraftRange = new GridBagConstraints();
        gbc_lblSpacecraftRange.anchor = GridBagConstraints.WEST;
        gbc_lblSpacecraftRange.insets = new Insets(0, 0, 0, 5);
        gbc_lblSpacecraftRange.gridx = 0;
        gbc_lblSpacecraftRange.gridy = 2;
        searchPropertiesPanel.add(lblSpacecraftRange, gbc_lblSpacecraftRange);

        minSCRange = new JFormattedTextField();
        GridBagConstraints gbc_minSCRange = new GridBagConstraints();
        gbc_minSCRange.fill = GridBagConstraints.HORIZONTAL;
        gbc_minSCRange.gridwidth = 2;
        gbc_minSCRange.insets = new Insets(0, 0, 5, 5);
        gbc_minSCRange.gridx = 1;
        gbc_minSCRange.gridy = 2;
        searchPropertiesPanel.add(minSCRange, gbc_minSCRange);
        minSCRange.setText("0");

        JLabel lblTo = new JLabel("to");
        GridBagConstraints gbc_lblTo = new GridBagConstraints();
        gbc_lblTo.insets = new Insets(0, 0, 5, 5);
        gbc_lblTo.gridx = 3;
        gbc_lblTo.gridy = 2;
        searchPropertiesPanel.add(lblTo, gbc_lblTo);

        maxSCRange = new JFormattedTextField();
        maxSCRange.setText("500");
        GridBagConstraints gbc_maxSCRange = new GridBagConstraints();
        gbc_maxSCRange.fill = GridBagConstraints.BOTH;
        gbc_maxSCRange.insets = new Insets(0, 0, 5, 5);
        gbc_maxSCRange.gridx = 4;
        gbc_maxSCRange.gridy = 2;
        searchPropertiesPanel.add(maxSCRange, gbc_maxSCRange);

        JLabel lblKm = new JLabel("km");
        GridBagConstraints gbc_lblKm = new GridBagConstraints();
        gbc_lblKm.insets = new Insets(0, 0, 5, 5);
        gbc_lblKm.gridx = 5;
        gbc_lblKm.gridy = 2;
        searchPropertiesPanel.add(lblKm, gbc_lblKm);

    }


    public JFormattedTextField getMinSCRange()
    {
        return minSCRange;
    }

    public void setMinSCRange(JFormattedTextField minSCRange)
    {
        this.minSCRange = minSCRange;
    }

    public JFormattedTextField getMaxSCRange()
    {
        return maxSCRange;
    }

    public void setMaxSCRange(JFormattedTextField maxSCRange)
    {
        this.maxSCRange = maxSCRange;
    }
}
