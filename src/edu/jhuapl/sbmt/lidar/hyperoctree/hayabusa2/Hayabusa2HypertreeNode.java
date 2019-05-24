package edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;

public class Hayabusa2HypertreeNode extends FSHyperTreeNode<Hayabusa2LidarPoint>
{
    final long bytesPerPoint=new FSHyperPointWithFileTag().getSizeInBytes();

    public Hayabusa2HypertreeNode(FSHyperTreeNode<Hayabusa2LidarPoint> parent, Path path,
            HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        super(parent, path, bbox, maxPoints, pool);
        writeBoundsFile();
    }

    @Override
    protected Hayabusa2HypertreeNode createNewChild(int i)
    {
        try
        {
            return new Hayabusa2HypertreeNode(this, getChildPath(i), getChildBounds(i), maxPoints, pool);
        }
        catch (HyperDimensionMismatchException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Hayabusa2LidarPoint createNewPoint(DataInputStream stream)
    {
        try
        {
            return new Hayabusa2LidarPoint(stream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getNumberOfPoints()
    {
        return getDataFilePath().toFile().length()/bytesPerPoint;
    }


}
