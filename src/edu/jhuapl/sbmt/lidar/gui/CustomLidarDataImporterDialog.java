/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.LidarDataSource;


public class CustomLidarDataImporterDialog extends javax.swing.JDialog
{
    private boolean okayPressed = false;
    private boolean isEditMode;
    private static final String LEAVE_UNMODIFIED = "<leave unmodified or empty to use existing plate data>";
    private String origOlaDatasourcePath; // used in Edit mode only to store original filename

    /** Creates new form ShapeModelImporterDialog */
    public CustomLidarDataImporterDialog(java.awt.Window parent, boolean isEditMode)
    {
        super(parent, "Import Lidar Data Source", Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        this.isEditMode = isEditMode;
    }

    /**
     * Set the cell data info
     */
    public void setLidarDatasourceInfo(LidarDataSource info)
    {
        if (isEditMode)
        {
            cellDataPathTextField.setText(LEAVE_UNMODIFIED);
            origOlaDatasourcePath = info.getPath();
        }

        nameTextField.setText(info.getName());
    }

    /**
     * @return
     */
    public LidarDataSource getLidarDatasourceInfo()
    {
       String name = nameTextField.getText();

       String path = cellDataPathTextField.getText();
        if (isEditMode &&
                (LEAVE_UNMODIFIED.equals(path) || path == null || path.isEmpty()))
        {
            path = origOlaDatasourcePath;
        }

        LidarDataSource retInfo = new LidarDataSource(name, path);
        return retInfo;
    }

    private String validateInput()
    {
        String result = null;

        String cellDataPath = cellDataPathTextField.getText();
        if (cellDataPath == null)
            cellDataPath = "";

        if (!isEditMode || (!cellDataPath.isEmpty() && !cellDataPath.equals(LEAVE_UNMODIFIED)))
        {
            if (cellDataPath.isEmpty())
                return "Please enter the path to the Lidar data source file.";

            File file = new File(cellDataPath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return cellDataPath + " does not exist or is not readable.";

            if (cellDataPath.contains(","))
                return "Lidar data source path may not contain commas.";

            if (cellDataPath.toLowerCase().endsWith(".fit") || cellDataPath.toLowerCase().endsWith(".fits") )
                result = validateFitsFile(cellDataPath);
            else
                result = validateOlaDatasourceDirectory(cellDataPath);

            if (result != null)
                return result;
        }

        String name = nameTextField.getText();
        if (name == null)
            name = "";
        name = name.trim();
        nameTextField.setText(name);
        if (name.isEmpty())
            return "Please enter a name for the Lidar data source.";


        return null;
    }

    private String validateOlaDatasourceDirectory(String cellDataPath)
    {
//        InputStream fs;
//        try
//        {
//            fs = new FileInputStream(cellDataPath);
//        }
//        catch (FileNotFoundException e)
//        {
//            return "The file '" + cellDataPath + "' does not exist or is not readable.";
//        }
//
//        InputStreamReader isr = new InputStreamReader(fs);
//        BufferedReader in = new BufferedReader(isr);
//
//        String line;
//        int lineCount = 0;
//        try
//        {
//            while ((line = in.readLine()) != null)
//            {
//                Double.parseDouble(line);
//                ++lineCount;
//            }
//
//            in.close();
//        }
//        catch (NumberFormatException e)
//        {
//            return "Numbers in file '" + cellDataPath + "' are malformatted.";
//        }
//        catch (IOException e)
//        {
//            return "An error occurred reading the file '" + cellDataPath + "'.";
//        }
//
//        if (lineCount != numCells)
//        {
//            return "Number of lines in file '" + cellDataPath + "' must equal number of plates in shape model.";
//        }
//
        return null;
    }

    private String validateFitsFile(String filename)
    {
        String result = null;
////        String result = "Ancillary FITS file reading not implemented yet";
//        String result = null;
//
//        try {
//            Fits fits = new Fits(filename);
//            BasicHDU[] hdus = fits.read();
//            int nhdus = fits.getNumberOfHDUs();
//            if (nhdus != 2)
//                return "FITS Ancillary File has improper number of HDUs";
//
//                BasicHDU hdu = fits.getHDU(1);
//                 if (hdu instanceof AsciiTableHDU)
//                {
//                    AsciiTableHDU athdu = (AsciiTableHDU)hdu;
//                    int ncols = athdu.getNCols();
//                    if (ncols <= SmallBodyModel.FITS_SCALAR_COLUMN_INDEX)
//                        return "FITS Ancillary File Has Insufficient Columns";
//
////                    for (int k=0; k<ncols; k++)
////                        System.out.print(athdu.getColumnName(k) + ", ");
////
////                    String scalarHeader = athdu.getColumnName(SmallBodyModel.FITS_SCALAR_COLUMN_INDEX);
////                    float[] scalars = (float[])athdu.getColumn(FITS_SCALAR_COLUMN_INDEX);
////                    for (int j=0; j<10; j++)
////                    {
////                        System.out.println("Value " + j + ": " + scalars[j]);
////                    }
//                }
//                else
//                    return "FITS Ancillary File doesn't have an Ascii Table HDU";
//
//        } catch (Exception e) { return "Error Parsing FITS Ancillary File"; }

        return result;
    }

    public boolean getOkayPressed()
    {
        return okayPressed;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pathLabel2 = new javax.swing.JLabel();
        cellDataPathTextField = new javax.swing.JTextField();
        browsePlateDataButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 167));
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        pathLabel2.setText("Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(pathLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(cellDataPathTextField, gridBagConstraints);

        browsePlateDataButton.setText("Browse...");
        browsePlateDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browsePlateDataButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browsePlateDataButton, gridBagConstraints);

        nameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(nameLabel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(nameTextField, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browsePlateDataButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browsePlateDataButtonActionPerformed
    {//GEN-HEADEREND:event_browsePlateDataButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Lidar Data Source",
                new ArrayList<String>(Arrays.asList("lidar")));
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        cellDataPathTextField.setText(filename);
    }//GEN-LAST:event_browsePlateDataButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        String errorString = validateInput();
        if (errorString != null)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    errorString,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        okayPressed = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browsePlateDataButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField cellDataPathTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pathLabel2;
    // End of variables declaration//GEN-END:variables
}
