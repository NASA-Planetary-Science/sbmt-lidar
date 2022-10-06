package edu.jhuapl.sbmt.lidar.misc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import vtk.vtkCellArray;
import vtk.vtkHexahedron;
import vtk.vtkPoints;
import vtk.vtkStringArray;
import vtk.vtkUnstructuredGrid;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaPointList;

public class FileSystemOctreeGenerator
{

	final Path nearsdcRootDirectory;
	final Path fullyResolvedOutputDirectory;
	final int maxNumberOfPointsPerLeaf;
	final BoundingBox boundingBox;
	final int maxNumberOfOpenFiles;
	FileSystemOctreeNode root;
	final DataOutputStreamPool streamManager;
	long totalPointsWritten = 0;

	public FileSystemOctreeGenerator(Path nearsdcRootDirectory, Path relativeTreeConstructionDirectory,
			int maxNumberOfPointsPerLeaf, BoundingBox bbox, int maxNumFiles) throws IOException
	{
		this.fullyResolvedOutputDirectory = nearsdcRootDirectory.resolve(relativeTreeConstructionDirectory);
		this.nearsdcRootDirectory = nearsdcRootDirectory;
		this.maxNumberOfPointsPerLeaf = maxNumberOfPointsPerLeaf;
		boundingBox = bbox;
		maxNumberOfOpenFiles = maxNumFiles;
		streamManager = new DataOutputStreamPool(maxNumberOfOpenFiles);
		root = new FileSystemOctreeNode(fullyResolvedOutputDirectory, bbox, maxNumberOfPointsPerLeaf, streamManager);
		root.writeBounds();
	}

	public void addPointsFromFileToRoot(Path inputFilePath) throws IOException
	{
		addPointsFromFileToRoot(inputFilePath, Integer.MAX_VALUE);
	}

	public void addPointsFromFileToRoot(Path inputFilePath, int nmax) throws IOException
	{ // first add all points to the root node, then expand the tree
		OlaPointList pointList = new OlaPointList();
		pointList.appendFromFile(inputFilePath);
		int limit = Math.min(pointList.getNumberOfPoints(), nmax);
		for (int i = 0; i < limit; i++)
		{
			if ((i % 200000) == 0)
				System.out.println((int) ((double) i / (double) pointList.getNumberOfPoints() * 100) + "% complete : " + i
						+ "/" + limit);
			root.addPoint(pointList.getPoint(i));
			totalPointsWritten++;
		}
	}

	public void expand() throws IOException
	{
		expandNode(root);
	}

	public void expandNode(FileSystemOctreeNode node) throws IOException
	{ // depth-first recursion, so we limit the number of open output files to 8
		System.out.println(node.getSelfPath() + " " + convertBytesToMB(node.getDataFilePath().toFile().length()) + " MB");
		if (node.numPoints > maxNumberOfPointsPerLeaf)
		{
			node.split();
			for (int i = 0; i < 8; i++)
				if (node.children[i] != null)
					expandNode(node.children[i]);
		}
	}

	public int getNumberOfNodes()
	{
		return getAllNodes().size();
	}

	public long getNumberOfBytes()
	{
		List<FileSystemOctreeNode> nodeList = getAllNonEmptyLeafNodes();
		long total = 0;
		for (FileSystemOctreeNode node : nodeList)
		{
			total += node.getDataFilePath().toFile().length();
		}
		return total;
	}

	public double convertBytesToMB(long bytes)
	{
		return (double) bytes / (double) (1024 * 1024);
	}

	public void writeStatistics(Path outputFilePath)
	{
		try
		{
			FileWriter writer = new FileWriter(outputFilePath.toFile());
			List<FileSystemOctreeNode> nodeList = getAllNonEmptyLeafNodes();
			for (FileSystemOctreeNode node : nodeList)
			{
				writer.write(String.valueOf(convertBytesToMB(node.getSelfPath().toFile().length())));
			}
			writer.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<FileSystemOctreeNode> getAllNodes()
	{
		List<FileSystemOctreeNode> nodeList = Lists.newArrayList();
		getAllNodes(root, nodeList);
		return nodeList;
	}

	void getAllNodes(FileSystemOctreeNode node, List<FileSystemOctreeNode> nodeList)
	{
		nodeList.add(node);
		for (int i = 0; i < 8; i++)
			if (node.children[i] != null)
				nodeList.add(node.children[i]);
	}

	public vtkUnstructuredGrid getAllNonEmptyLeavesAsUnstructuredGrid()
	{
		List<FileSystemOctreeNode> nodeList = getAllNonEmptyLeafNodes();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		vtkStringArray paths = new vtkStringArray();
		for (FileSystemOctreeNode node : nodeList)
		{
			vtkHexahedron hex = new vtkHexahedron();
			for (int i = 0; i < 8; i++)
			{
				Vector3D crn = node.getCorner(i);
				int id = (int)points.InsertNextPoint(crn.getX(), crn.getY(), crn.getZ());
				hex.GetPointIds().SetId(i, id);
			}
			cells.InsertNextCell(hex);
			String relativePath = "/" + node.getSelfPath().toString().replace(nearsdcRootDirectory.toString(), "");
			System.out.println(relativePath);
			paths.InsertNextValue(relativePath);
		}
		//
		vtkUnstructuredGrid grid = new vtkUnstructuredGrid();
		grid.SetPoints(points);
		grid.SetCells(new vtkHexahedron().GetCellType(), cells);
		grid.GetCellData().AddArray(paths);
		return grid;
	}

	public List<FileSystemOctreeNode> getAllNonEmptyLeafNodes()
	{
		List<FileSystemOctreeNode> nodeList = Lists.newArrayList();
		getAllNonEmptyLeafNodes(root, nodeList);
		return nodeList;
	}

	void getAllNonEmptyLeafNodes(FileSystemOctreeNode node, List<FileSystemOctreeNode> nodeList)
	{
		if (!node.isLeaf)
			for (int i = 0; i < 8; i++)
				getAllNonEmptyLeafNodes(node.children[i], nodeList);
		else if (node.numPoints > 0)
			nodeList.add(node);
	}

	public void commit() throws IOException
	{
		streamManager.closeAllStreams();// close any files that are still open
		finalCommit(root);
	}

	void finalCommit(FileSystemOctreeNode node) throws IOException
	{
		if (!node.isLeaf)
			for (int i = 0; i < 8; i++)
				finalCommit(node.children[i]);
		else
		{
			File dataFile = node.getDataFilePath().toFile(); // clean up any data
																				// files with zero
																				// points
			if (dataFile.length() == 0l)
				dataFile.delete();
		}
	}

	public long getTotalPointsWritten()
	{
		return totalPointsWritten;
	}

	public FileSystemOctreeNode getRoot()
	{
		return root;
	}

	public Path getFullyResolvedOutputDirectory()
	{
		return fullyResolvedOutputDirectory;
	}

	public Path getDataFilePath()
	{
		return root.getDataFilePath();
	}
}
