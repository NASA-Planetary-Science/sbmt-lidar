package edu.jhuapl.sbmt.lidar.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import edu.jhuapl.saavtk.model.LidarDataSource;

/**
 * ListCellRenderer used to handle DataSource objects.
 *
 * @author lopeznr1
 */
public class DataSourceRenderer extends DefaultListCellRenderer
{
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object aObj, int index, boolean isSelected,
			boolean hasFocus)
	{
		JLabel retL = (JLabel) super.getListCellRendererComponent(list, aObj, index, isSelected, hasFocus);

		if (aObj instanceof LidarDataSource)
		{
			LidarDataSource tmpSource = (LidarDataSource) aObj;
			retL.setText(tmpSource.getName());
		}

		return retL;
	}

}
