package edu.jhuapl.sbmt.lidar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaStreamUtil;

class FileSystemOctreeNode extends BoundingBox
{
	final Path selfPath;
	boolean isLeaf = true;
	FileSystemOctreeNode[] children = new FileSystemOctreeNode[8];
	int numPoints = 0;
	int maxPoints;
	DataOutputStreamPool streamPool;

	public Vector3D getCorner(int i)
	{
		switch (i)
		{
			case 0:
				return new Vector3D(xmin, ymin, zmin);
			case 1:
				return new Vector3D(xmax, ymin, zmin);
			case 2:
				return new Vector3D(xmax, ymax, zmin);
			case 3:
				return new Vector3D(xmin, ymax, zmin);
			case 4:
				return new Vector3D(xmin, ymin, zmax);
			case 5:
				return new Vector3D(xmax, ymin, zmax);
			case 6:
				return new Vector3D(xmax, ymax, zmax);
			case 7:
				return new Vector3D(xmin, ymax, zmax);
		}
		return null;
	}

	public double getVolume()
	{
		return (xmax - xmin) * (ymax - ymin) * (zmax - zmin);
	}

	/**
	 * Constructor used to create a root Node.
	 */
	public FileSystemOctreeNode(Path rootPath, BoundingBox bbox, int maxPoints, DataOutputStreamPool streamPool)
	{
		super(bbox.getBounds());
		selfPath = rootPath;
		getSelfPath().toFile().mkdir();
		this.maxPoints = maxPoints;
		this.streamPool = streamPool;
	}

	/**
	 * Constructor used to create a child Node
	 */
	FileSystemOctreeNode(FileSystemOctreeNode parent, int whichChild, int maxPoints, DataOutputStreamPool streamPool)
			throws IOException
	{
		super(createBoundingBox(parent, whichChild).getBounds());
		//
		selfPath = parent.getSelfPath().resolve(String.valueOf(whichChild));
		getSelfPath().toFile().mkdir();
		this.maxPoints = maxPoints;
		this.streamPool = streamPool;
	}

	boolean isInside(LidarPoint aLP)
	{
		Vector3D vec = aLP.getTargetPosition();
		return contains(new double[] { vec.getX(), vec.getY(), vec.getZ() });
	}

	boolean addPoint(LidarPoint aLP) throws IOException
	{
		if (!isLeaf)
		{
			for (int i = 0; i < 8; i++)
				if (children[i].addPoint(aLP))
					return true;
		}
		else
		{
			if (isInside(aLP))
			{
				OlaStreamUtil.writeLidarPointToStream(streamPool.getStream(getDataFilePath()), aLP);
				numPoints++;
				// if (numPoints>maxPoints)
				// split();
				return true;
			}
		}
		return false;
	}

	Path getSelfPath()
	{
		return selfPath;
	}

/*
 * Path getChildPath(int i) { return selfPath.resolve(String.valueOf(i)); }
 */

	Path getBoundsFilePath()
	{
		return getSelfPath().resolve("bounds");
	}

	void writeBounds() throws IOException
	{
		// System.out.println(getBoundsFilePath());
		DataOutputStream stream = new DataOutputStream(new FileOutputStream(getBoundsFilePath().toFile()));
		stream.writeDouble(xmin);
		stream.writeDouble(xmax);
		stream.writeDouble(ymin);
		stream.writeDouble(ymax);
		stream.writeDouble(zmin);
		stream.writeDouble(zmax);
		stream.close();
	}

	public double[] readBounds() throws IOException
	{
		DataInputStream stream = new DataInputStream(new FileInputStream(getBoundsFilePath().toFile()));
		double[] bounds = new double[6];
		for (int i = 0; i < 6; i++)
			bounds[i] = stream.readDouble();
		stream.close();
		return bounds;
	}

	Path getDataFilePath()
	{
		return getSelfPath().resolve("data");
	}

	void split() throws IOException
	{
		streamPool.closeStream(getDataFilePath());
		for (int i = 0; i < 8; i++)
			children[i] = new FileSystemOctreeNode(this, i, maxPoints, streamPool);
		//
		DataInputStream selfStream = new DataInputStream(new FileInputStream(getDataFilePath().toFile()));
		while (selfStream.skipBytes(0) == 0)
		{ // dirty trick to keep reading until EOF
			LidarPoint tmpLP = OlaStreamUtil.readLidarPointFromStream(selfStream);
			if (tmpLP == null)
				break;
			Vector3D pos = tmpLP.getTargetPosition();
			double[] p = new double[] { pos.getX(), pos.getY(), pos.getZ() };
			boolean found = false;
			for (int i = 0; i < 8 && !found; i++)
				if (children[i].contains(p))
				{
					// TODO: make sure > and < in the contains(...) method is not
					// falsely rejecting points on the boundary of the children boxes
					children[i].addPoint(tmpLP);
					found = true;
				}
		}
		//
		selfStream.close();
		//
		isLeaf = false;
		deleteDataFile();
	}

	void deleteDataFile()
	{
		getDataFilePath().toFile().delete();
	}

	static BoundingBox createBoundingBox(BoundingBox parent, int whichChild)
	{
		BoundingBox bbox = new BoundingBox(parent.getBounds());
		double xmid = bbox.getCenterPoint()[0];
		double ymid = bbox.getCenterPoint()[1];
		double zmid = bbox.getCenterPoint()[2];
		switch (whichChild)
		{
			case 0:
				bbox.xmax = xmid;
				bbox.ymax = ymid;
				bbox.zmax = zmid;
				break;
			case 1:
				bbox.xmin = xmid;
				bbox.ymax = ymid;
				bbox.zmax = zmid;
				break;
			case 2:
				bbox.xmin = xmid;
				bbox.ymin = ymid;
				bbox.zmax = zmid;
				break;
			case 3:
				bbox.xmax = xmid;
				bbox.ymin = ymid;
				bbox.zmax = zmid;
				break;
			case 4:
				bbox.xmax = xmid;
				bbox.ymax = ymid;
				bbox.zmin = zmid;
				break;
			case 5:
				bbox.xmin = xmid;
				bbox.ymax = ymid;
				bbox.zmin = zmid;
				break;
			case 6:
				bbox.xmin = xmid;
				bbox.ymin = ymid;
				bbox.zmin = zmid;
				break;
			case 7:
				bbox.xmax = xmid;
				bbox.ymin = ymid;
				bbox.zmin = zmid;
				break;
		}
		// System.out.println(bbox);
		return bbox;
	}

}