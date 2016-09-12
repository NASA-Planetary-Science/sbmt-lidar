package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.test.DataOutputStreamPool;

public abstract class FSHyperTreeGenerator
{
    protected final Path outputDirectory;
    protected final int maxNumberOfPointsPerLeaf;
    protected final HyperBox bbox;
    protected final int maxNumberOfOpenOutputFiles;
    protected final DataOutputStreamPool pool;
    protected FSHyperTreeNode root;
    protected long totalPointsWritten=0;

    protected BiMap<Path, Integer> fileMap=HashBiMap.create();

    public FSHyperTreeGenerator(Path outputDirectory, int maxNumberOfPointsPerLeaf, HyperBox bbox, int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        this.outputDirectory=outputDirectory;
        this.maxNumberOfPointsPerLeaf=maxNumberOfPointsPerLeaf;
        this.maxNumberOfOpenOutputFiles=maxNumberOfOpenOutputFiles;
        this.bbox=bbox;
        this.pool=pool;
        root=new OlaFSHyperTreeNode(null, outputDirectory, bbox, maxNumberOfPointsPerLeaf,pool);
    }

    public void expand() throws HyperException, IOException
    {
        expandNode(root);
    }

    public void expandNode(FSHyperTreeNode node) throws HyperException, IOException
    {
        if (node.getNumberOfPoints()>maxNumberOfPointsPerLeaf)
        {
            node.split();
            for (int i=0; i<node.getNumberOfChildren(); i++)
                if (node.childExists(i))
                {
                    System.out.println(node.getChild(i).getPath());
                    expandNode(node.getChild(i));
                }
        }
    }

    public void commit() throws IOException
    {
        pool.closeAllStreams();// close any files that are still open
        finalCommit(root);
    }

    void finalCommit(FSHyperTreeNode node) throws IOException
    {
        File dataFile=node.getDataFilePath().toFile();  // clean up any data files with zero points
        if (!node.isLeaf)
        {
            if (dataFile.exists())
                dataFile.delete();
            for (int i=0; i<8; i++)
                finalCommit((FSHyperTreeNode)node.getChild(i));
        }
        else {
            if (!dataFile.exists() || dataFile.length()==0l)
            {
                node.getBoundsFilePath().toFile().delete();
                node.getPath().toFile().delete();
            }
        }
    }

    public double convertBytesToMB(long bytes)
    {
        return (double)bytes/(double)(1024*1024);
    }

    public long countBytes()
    {
        List<FSHyperTreeNode> nodeList=getAllNonEmptyLeafNodes();
        long total=0;
        for (FSHyperTreeNode node : nodeList)
            total+=node.getDataFilePath().toFile().length();
        return total;
    }

    public List<FSHyperTreeNode> getAllNonEmptyLeafNodes()
    {
        List<FSHyperTreeNode> nodeList=Lists.newArrayList();
        getAllNonEmptyLeafNodes(root, nodeList);
        return nodeList;
    }

    void getAllNonEmptyLeafNodes(FSHyperTreeNode node, List<FSHyperTreeNode> nodeList)
    {
        if (!node.isLeaf)
            for (int i=0; i<node.getNumberOfChildren(); i++)
                getAllNonEmptyLeafNodes((FSHyperTreeNode)node.getChild(i), nodeList);
        else if (node.getDataFilePath().toFile().exists())
            nodeList.add(node);
    }



}
