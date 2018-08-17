package edu.jhuapl.sbmt.lidar.hyperoctree;

import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.HyperBoundedObject;

public class HyperBox implements Dimensioned
{
    double[] min,max,mid;
    int dim;

    public HyperBox(double[] min, double[] max) throws HyperDimensionMismatchException
    {
        if (min.length!=max.length)
            throw new HyperDimensionMismatchException(min.length,max.length);
        this.min=min;
        this.max=max;
        mid=new double[min.length];
        for (int i=0; i<min.length; i++)
            mid[i]=(min[i]+max[i])/2.;
        dim=min.length;
    }

    public HyperBox(HyperBox box)
    {
        min=new double[box.dim];
        max=new double[box.dim];
        mid=new double[box.dim];
        for (int i=0; i<box.dim; i++)
        {
            min[i]=box.min[i];
            max[i]=box.max[i];
            mid[i]=box.mid[i];
        }
        dim=box.dim;
    }

    @Override
    public int getDimension()
    {
        return dim;
    }

    public double[] getMin()
    {
        return min;
    }

    public double[] getMax()
    {
        return max;
    }

    public double[] getMid()
    {
        return mid;
    }

    public double[] getBounds()
    {
        double[] bounds=new double[dim*2];
        for (int i=0; i<dim; i++)
        {
            bounds[2*i+0]=getMin()[i];
            bounds[2*i+1]=getMax()[i];
        }
        return bounds;
    }

    public boolean contains(HyperPoint value) throws HyperException
    {
        if (value.getDimension()!=this.getDimension())
            throw new HyperDimensionMismatchException(value.getDimension(),this.getDimension());
        for (int i=0; i<getDimension(); i++)
            if (value.getCoordinate(i)<min[i] || value.getCoordinate(i)>max[i])
                return false;
        return true;
    }

    public boolean intersects(HyperBox box) throws HyperException
    {
        if (box.getDimension()!=this.getDimension())
            throw new HyperDimensionMismatchException(box.getDimension(),this.getDimension());
        for (int i=0; i<getDimension(); i++)
            if (box.min[i]>this.max[i] || box.max[i]<this.min[i])
                return false;
        return true;
    }

    @Override
    public String toString()
    {
        String out="HyperBox{"+getDimension()+"}";
        for (int i=0; i<getDimension(); i++)
            out+="["+min[i]+","+max[i]+"]";
        return out;
    }

    // TODO - check if image is contained at all in the hyperbox
    //
    public boolean contains(HyperBoundedObject image)
    {
        for (int i=0; i < getDimension(); i++)
            if (image.getCoordinate(i)<min[i] || image.getCoordinate(i)>max[i])
                return false;
        return true;
    }
}
