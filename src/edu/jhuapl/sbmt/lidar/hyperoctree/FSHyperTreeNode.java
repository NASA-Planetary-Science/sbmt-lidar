package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;


public abstract class FSHyperTreeNode<T extends FSHyperPoint> implements Dimensioned
{
    Path path;
    protected DataOutputStreamPool pool;
    HyperBox bbox;
    boolean isLeaf=true;
    protected int maxPoints;
    int numPoints;
    FSHyperTreeNode<T>[] children;
    FSHyperTreeNode<T> parent;

    public FSHyperTreeNode(FSHyperTreeNode<T> parent, Path path, HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        this.parent=parent;
        this.path=path;
        this.bbox=bbox;
        this.pool=pool;
        this.maxPoints=maxPoints;
        children=new FSHyperTreeNode[(int)Math.pow(2, bbox.getDimension())];
        for (int i=0; i<children.length; i++)
            children[i]=null;
        path.toFile().mkdir();
    }

    public Path getPath()
    {
        return path;
    }

    public Path getChildPath(int i)
    {
        return path.resolve(String.valueOf(i));
    }

    public Path getDataFilePath()
    {
        return path.resolve("data");
    }

    public Path getBoundsFilePath()
    {
        return path.resolve("bounds");
    }

    public void writeBoundsFile()
    {
        try
        {
            DataOutputStream stream=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getBoundsFilePath().toFile())));
            double[] min=bbox.getMin();
            double[] max=bbox.getMax();
            for (int i=0; i<bbox.getDimension(); i++)
            {
                stream.writeDouble(min[i]);
                stream.writeDouble(max[i]);
            }
            stream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static double[] readBoundsFile(Path boundsFilePath, int dimension)

    {
        double[] bounds=new double[dimension*2];
        try
        {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(boundsFilePath.toFile())));
            for (int i=0; i<dimension; i++)
            {
                bounds[2*i+0]=stream.readDouble();
                bounds[2*i+1]=stream.readDouble();
            }
            stream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bounds;
    }

    void deleteDataFile() {
        getDataFilePath().toFile().delete();
    }

    public int getNumberOfChildren()
    {
        return children.length;
    }

    public boolean childExists(int i)
    {
        return children[i]!=null;
    }

    private int selectChild(T pt)
    {
        int idx=0;
        for (int i=0; i<getDimension(); i++)
        {
            int whichBit=(int)Math.pow(2,i);
            idx+=pt.getCoordinate(i)<bbox.getMid()[i]?0:whichBit;
        }
        return idx;
    }

    public HyperBox getBoundingBox()
    {
        return bbox;
    }

    protected HyperBox getChildBounds(int i) throws HyperDimensionMismatchException
    {
        double[] min=new double[getDimension()];
        double[] max=new double[getDimension()];
        for (int j=0; j<getDimension(); j++)
        {
            int whichBit=(int)Math.pow(2,j);
            if ((i&whichBit)>>j==0)
            {
                min[j]=bbox.getMin()[j];
                max[j]=bbox.getMid()[j];
            }
            else
            {
                min[j]=bbox.getMid()[j];
                max[j]=bbox.getMax()[j];
            }
        }
        return new HyperBox(min, max);
    }

    public boolean add(T pt) throws HyperException, IOException
    {
        if (!isLeaf) {
            for (int i=0; i<getNumberOfChildren(); i++)
                if (children[i].add(pt))
                    return true;
        } else {
            if (isInside(pt)) {
                pt.write(pool.getStream(getDataFilePath()));
                numPoints++;
           //     if (numPoints>maxPoints)
           //         trySplit();
                return true;
            }
        }
        return false;

    }

    public boolean isInside(T pt) throws HyperException
    {
        return bbox.contains(pt);
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

    protected abstract long getNumberOfPoints();
    protected abstract <S extends FSHyperTreeNode> S createNewChild(int i);
    protected abstract T createNewPoint(DataInputStream stream);

    public void split() throws HyperException, IOException
    {
        pool.closeStream(getDataFilePath());
        for (int i=0; i<getNumberOfChildren(); i++)
            children[i]=createNewChild(i);
        DataInputStream instream=new DataInputStream(new BufferedInputStream(new FileInputStream(getDataFilePath().toFile())));
        while (instream.available()>0)
        {
            T pt=createNewPoint(instream);
            for (int i=0; i<getNumberOfChildren() ; i++)
                if (children[i].getBoundingBox().contains(pt))
                {
                    children[i].add(pt);
                    break;
                }
        }
        instream.close();
        isLeaf=false;
        deleteDataFile();
    }

    public FSHyperTreeNode<T> getChild(int i)
    {
        return children[i];
    }

    @Override
    public int getDimension()
    {
        return bbox.getDimension();
    }


}
