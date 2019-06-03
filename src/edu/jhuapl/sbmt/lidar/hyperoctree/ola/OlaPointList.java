package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.lidar.BasicLidarPoint;
import edu.jhuapl.sbmt.lidar.LidarPointList;

public class OlaPointList implements LidarPointList
{
	List<BasicLidarPoint> points = Lists.newArrayList();

	@Override
	public int getNumberOfPoints()
	{
		return points.size();
	}

	@Override
	public BasicLidarPoint getPoint(int i)
	{
		return points.get(i);
	}

	@Override
	public void clear()
	{
		points.clear();
	}

	public BoundingBox getBoundingBoxForTargetPoints()
	{
		double xmin = Double.MAX_VALUE;
		double xmax = Double.MIN_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = Double.MIN_VALUE;
		double zmin = Double.MAX_VALUE;
		double zmax = Double.MIN_VALUE;
		for (int i = 0; i < getNumberOfPoints(); i++)
		{
			double x = getPoint(i).getTargetPosition().getX();
			double y = getPoint(i).getTargetPosition().getY();
			double z = getPoint(i).getTargetPosition().getZ();
			if (x < xmin)
				xmin = x;
			if (x > xmax)
				xmax = x;
			if (y < ymin)
				ymin = y;
			if (y > ymax)
				ymax = y;
			if (z < zmin)
				zmin = z;
			if (z > zmax)
				zmax = z;
		}
		return new BoundingBox(new double[] { xmin, xmax, ymin, ymax, zmin, zmax });
	}

	@Override
	public void appendFromFile(Path l2FilePath)
	{
		try
		{
			String filePathString = l2FilePath.toString();
			if (!filePathString.endsWith(".l2"))
				throw new Exception("Incorrect file extension \""
						+ filePathString.substring(filePathString.lastIndexOf('.')) + "\" expected .l2");

			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(l2FilePath.toFile())));

			while (true)
			{
				boolean noise = false;
				Vector3D scpos = null;
				Vector3D tgpos = null;
				double time = 0;
				double intensity = 0;
				double range = 0;
				double x, y, z;

				try
				{
					in.readByte();
				}
				catch (EOFException e)
				{
					break;
				}

				try
				{
					in.skipBytes(17 + 8 + 24);
					time = FileUtil.readDoubleAndSwap(in);
					in.skipBytes(8 + 2 * 3);
					short flagStatus = MathUtil.swap(in.readShort());
					noise = ((flagStatus == 0 || flagStatus == 1) ? false : true);
					in.skipBytes(8 + 8 * 4);
					x = FileUtil.readDoubleAndSwap(in) / 1000.0;
					y = FileUtil.readDoubleAndSwap(in) / 1000.0;
					z = FileUtil.readDoubleAndSwap(in) / 1000.0;
					tgpos = new Vector3D(x, y, z);
					in.skipBytes(8 * 3);
					x = FileUtil.readDoubleAndSwap(in) / 1000.0;
					y = FileUtil.readDoubleAndSwap(in) / 1000.0;
					z = FileUtil.readDoubleAndSwap(in) / 1000.0;
					scpos = new Vector3D(x, y, z);
				}
				catch (IOException e)
				{
					in.close();
					throw e;
				}

				if (!noise)
				{
					this.points.add(new BasicLidarPoint(scpos, tgpos, time, range, intensity));
				}
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void appendFromTreeFilePath(Path treeFilePath)
	{
		try
		{
			DataInputStream stream = new DataInputStream(new FileInputStream(treeFilePath.toFile()));
			while (stream.skipBytes(0) == 0)
			{ // dirty trick to keep reading until EOF
				BasicLidarPoint tmpLP = OlaStreamUtil.readLidarPointFromStream(stream);
				if (tmpLP == null)
					break;
				this.points.add(tmpLP);
			}
			stream.close();
		}
		catch (EOFException e)
		{

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
