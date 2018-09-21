package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperPoint;

public class Hayabusa2LaserHypertreeNode extends FSHyperTreeNode<Hayabusa2LaserLidarPoint>
{
    final long bytesPerPoint=new OlaFSHyperPoint().getSizeInBytes();

    public Hayabusa2LaserHypertreeNode(FSHyperTreeNode<Hayabusa2LaserLidarPoint> parent, Path path,
            HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        super(parent, path, bbox, maxPoints, pool);
        writeBoundsFile();
    }

    @Override
    protected Hayabusa2LaserHypertreeNode createNewChild(int i)
    {
        try
        {
            return new Hayabusa2LaserHypertreeNode(this, getChildPath(i), getChildBounds(i), maxPoints, pool);
        }
        catch (HyperDimensionMismatchException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Hayabusa2LaserLidarPoint createNewPoint(DataInputStream stream)
    {
        try
        {
            return new Hayabusa2LaserLidarPoint(stream);
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
