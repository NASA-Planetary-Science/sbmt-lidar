package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.sbmt.lidar.BasicLidarPoint;
import edu.jhuapl.sbmt.lidar.hyperoctree.OctreePoint;

public class OlaOctreePoint extends BasicLidarPoint implements OctreePoint {

    boolean fullyRead=false;

    public OlaOctreePoint(DataInputStream stream)
    {
        super(Vector3D.ZERO, Vector3D.ZERO, 0, 0);
        try
        {
            readFromStream(stream);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public OlaOctreePoint(BasicLidarPoint point)
    {
        super(point.getSourcePosition(), point.getTargetPosition(), point.getTime(), point.getIntensityReceived());
    }

    @Override
    public Vector3D getPosition()
    {
        return tgpos;
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException
    {
        stream.writeDouble(time);
        stream.writeDouble(tgpos.getX());
        stream.writeDouble(tgpos.getY());
        stream.writeDouble(tgpos.getZ());
        stream.writeDouble(intensity);
        stream.writeDouble(scpos.getX());
        stream.writeDouble(scpos.getY());
        stream.writeDouble(scpos.getZ());
    }

    @Override
    public void readFromStream(DataInputStream stream) throws IOException
    {
        try {
            time=stream.readDouble();
            tgpos=new Vector3D(stream.readDouble(),stream.readDouble(),stream.readDouble());
            intensity=stream.readDouble();
            scpos=new Vector3D(stream.readDouble(),stream.readDouble(),stream.readDouble());
        } catch (EOFException e){
            return;
        }
        fullyRead=true;
    }

    @Override
    public boolean isFullyRead() {
        return fullyRead;
    }

}
