package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperPoint;
import edu.jhuapl.sbmt.lidar.test.DataOutputStreamPool;

public class MolaFSHyperTreeNode extends FSHyperTreeNode<MolaFSHyperPoint>
{

    final long bytesPerPoint=new OlaFSHyperPoint().getSizeInBytes();

    public MolaFSHyperTreeNode(FSHyperTreeNode<MolaFSHyperPoint> parent,
            Path path, HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        super(parent, path, bbox, maxPoints, pool);
        writeBoundsFile();
    }

    @Override
    public long getNumberOfPoints()
    {
        return getDataFilePath().toFile().length()/bytesPerPoint;
    }

    @Override
    public MolaFSHyperTreeNode createNewChild(int i)
    {
        try
        {
            return new MolaFSHyperTreeNode(this, getChildPath(i), getChildBounds(i), maxPoints, pool);
        }
        catch (HyperDimensionMismatchException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MolaFSHyperPoint createNewPoint(DataInputStream stream)
    {
        try
        {
            return new MolaFSHyperPoint(stream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
