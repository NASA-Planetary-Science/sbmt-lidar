package edu.jhuapl.sbmt.lidar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.util.TimeUtil;

public class LidarPlot extends JFrame implements ChartMouseListener
{
	// Ref vars
	private final LidarTrackManager refModel;
	private final LidarTrack refTrack;

	// Plot vars
	private final List<Double> dataL;
	private final List<Double> distanceL;
	private final List<Double> timeL;
	private final String name;

	private XYDataset distanceDataset;
	private XYDataset timeDataset;
	private XYSeries distanceDataSeries;
	private XYSeries timeDataSeries;
	private XYSeries distanceSelectionSeries;
	private XYSeries timeSelectionSeries;

	public LidarPlot(LidarTrackManager aModel, LidarTrack aTrack, List<Double> aDataL, List<Double> aDistanceL,
			List<Double> aTimeL, String aName, String aUnits)
	{
		refModel = aModel;
		refTrack = aTrack;

		dataL = aDataL;
		distanceL = aDistanceL;
		timeL = aTimeL;
		name = aName;

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());

		{
			distanceSelectionSeries = new XYSeries("Lidar Selection");
			distanceDataSeries = new XYSeries("Lidar Data");

			distanceDataset = new XYSeriesCollection();
			((XYSeriesCollection) distanceDataset).addSeries(distanceSelectionSeries);
			((XYSeriesCollection) distanceDataset).addSeries(distanceDataSeries);

			final JFreeChart chart1 = ChartFactory.createXYLineChart(name + " vs. Distance", "Distance (km)",
					name + " (" + aUnits + ")", distanceDataset, PlotOrientation.VERTICAL, false, true, false);

			// add the jfreechart graph
			ChartPanel chartPanel = new ChartPanel(chart1) {
				@Override
				public void restoreAutoRangeBounds()
				{
					super.restoreAutoRangeBounds();
					// This makes sure when the user auto-range's the plot, it will
					// bracket the
					// well with a small margin
					((XYPlot) chart1.getPlot()).getRangeAxis().setRangeWithMargins(distanceDataSeries.getMinY(),
							distanceDataSeries.getMaxY());
				}
			};
			chartPanel.setMouseWheelEnabled(true);
			chartPanel.addChartMouseListener(this);

			XYPlot plot = (XYPlot) chart1.getPlot();
			plot.setDomainPannable(true);
			plot.setRangePannable(true);

			XYItemRenderer r = plot.getRenderer();
			if (r instanceof XYLineAndShapeRenderer)
			{
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				renderer.setDefaultShapesVisible(true);
				renderer.setDefaultShapesFilled(true);
				renderer.setDrawSeriesLineAsPath(true);
				renderer.setSeriesPaint(0, Color.BLACK);
				renderer.setSeriesPaint(1, Color.RED);
			}

			panel.add(chartPanel, BorderLayout.CENTER);

			distanceDataSeries.clear();
			if (aDataL.size() > 0 && aDistanceL.size() > 0)
			{
				for (int i = 0; i < aDataL.size(); ++i)
					distanceDataSeries.add(aDistanceL.get(i), aDataL.get(i), false);
			}
			distanceDataSeries.fireSeriesChanged();

			plot.getRangeAxis().setRangeWithMargins(distanceDataSeries.getMinY(), distanceDataSeries.getMaxY());
		}

		{
			timeSelectionSeries = new XYSeries("Lidar Selection");
			timeDataSeries = new XYSeries("Lidar Data");

			timeDataset = new XYSeriesCollection();
			((XYSeriesCollection) timeDataset).addSeries(timeSelectionSeries);
			((XYSeriesCollection) timeDataset).addSeries(timeDataSeries);

			final JFreeChart chart2 = ChartFactory.createXYLineChart(name + " vs. Time", "Time (sec)",
					name + " (" + aUnits + ")", timeDataset, PlotOrientation.VERTICAL, false, true, false);

			// add the jfreechart graph
			ChartPanel chartPanel = new ChartPanel(chart2) {
				@Override
				public void restoreAutoRangeBounds()
				{
					super.restoreAutoRangeBounds();
					// This makes sure when the user auto-range's the plot, it will
					// bracket the
					// well with a small margin
					((XYPlot) chart2.getPlot()).getRangeAxis().setRangeWithMargins(timeDataSeries.getMinY(),
							timeDataSeries.getMaxY());
				}
			};
			chartPanel.setMouseWheelEnabled(true);
			chartPanel.addChartMouseListener(this);

			XYPlot plot = (XYPlot) chart2.getPlot();
			plot.setDomainPannable(true);
			plot.setRangePannable(true);

			XYItemRenderer r = plot.getRenderer();
			if (r instanceof XYLineAndShapeRenderer)
			{
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				renderer.setDefaultShapesVisible(true);
				renderer.setDefaultShapesFilled(true);
				renderer.setDrawSeriesLineAsPath(true);
				renderer.setSeriesPaint(0, Color.BLACK);
				renderer.setSeriesPaint(1, Color.RED);
			}

			panel.add(chartPanel, BorderLayout.SOUTH);

			timeDataSeries.clear();
			if (aDataL.size() > 0 && aTimeL.size() > 0)
			{
				double t0 = aTimeL.get(0);
				for (int i = 0; i < aDataL.size(); ++i)
					timeDataSeries.add(aTimeL.get(i) - t0, aDataL.get(i), false);
			}
			timeDataSeries.fireSeriesChanged();

			plot.getRangeAxis().setRangeWithMargins(timeDataSeries.getMinY(), timeDataSeries.getMaxY());
		}

		add(panel, BorderLayout.CENTER);

		createMenus();

		String title = "Trk " + aTrack.getId() + ": " + name;
		setTitle(title);
		pack();
		setVisible(true);
	}

	private void createMenus()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem mi = new JMenuItem(new ExportDataAction());
		fileMenu.add(mi);

		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	public void selectPoint(int ptId)
	{
		distanceSelectionSeries.clear();
		if (ptId >= 0)
		{
			distanceSelectionSeries.add(distanceDataSeries.getX(ptId), distanceDataSeries.getY(ptId), true);
		}

		timeSelectionSeries.clear();
		if (ptId >= 0)
		{
			timeSelectionSeries.add(timeDataSeries.getX(ptId), timeDataSeries.getY(ptId), true);
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent arg0)
	{
		ChartEntity entity = arg0.getEntity();

		if (entity instanceof XYItemEntity)
		{
			int id = ((XYItemEntity) entity).getItem();
			selectPoint(id);

			LidarPoint tmpPoint = refTrack.getPointList().get(id);
			refModel.setSelectedPoint(refTrack, tmpPoint);
		}
		else
		{
			distanceSelectionSeries.clear();
			timeSelectionSeries.clear();
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0)
	{
	}

	private class ExportDataAction extends AbstractAction
	{
		public ExportDataAction()
		{
			super("Export Data...");
		}

		public void actionPerformed(ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(LidarPlot.this, "Export Data", name + ".txt");
			if (file == null)
				return;

			String newline = System.getProperty("line.separator");
			try (BufferedWriter out = new BufferedWriter(new FileWriter(file));)
			{
				out.write(name + " Distance Time" + newline);

				int size = dataL.size();
				for (int i = 0; i < size; ++i)
				{
					out.write(dataL.get(i) + " " + distanceL.get(i) + " " + TimeUtil.et2str(timeL.get(i)) + newline);
				}
				out.close();
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(LidarPlot.this),
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}

		}
	}

}
