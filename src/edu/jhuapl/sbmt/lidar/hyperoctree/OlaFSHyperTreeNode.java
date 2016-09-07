package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.sbmt.lidar.test.DataOutputStreamPool;

public class OlaFSHyperTreeNode extends FSHyperTreeNode<OlaFSHyperPoint>
{
    final long bytesPerPoint=new OlaFSHyperPoint().getSizeInBytes();

    public OlaFSHyperTreeNode(FSHyperTreeNode<OlaFSHyperPoint> parent, Path path,
            HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        super(parent, path, bbox, maxPoints, pool);
        writeBoundsFile();
    }

    @Override
    protected OlaFSHyperTreeNode createNewChild(int i)
    {
        try
        {
            return new OlaFSHyperTreeNode(this, getChildPath(i), getChildBounds(i), maxPoints, pool);
        }
        catch (HyperDimensionMismatchException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected OlaFSHyperPoint createNewPoint(DataInputStream stream)
    {
        try
        {
            return new OlaFSHyperPoint(stream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected long getNumberOfPoints()
    {
        return getDataFilePath().toFile().length()/bytesPerPoint;
    }


}
