package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSLidarHyperPoint;
import edu.jhuapl.sbmt.lidar.test.LidarPoint;

public class MolaFSHyperPoint implements FSLidarHyperPoint
{



    // there are 6 data values but only 4 are used to define the hyperspace: tgx,tgy,tgz,time
    double[] data=new double[6];    // tgx,tgy,tgz,time,intensity,range
    int fileNum;

    public MolaFSHyperPoint()
    {
        // TODO Auto-generated constructor stub
    }

    public MolaFSHyperPoint(double tgx, double tgy, double tgz, double time, double intensity, double range, int fileNum)
    {
        data[0]=tgx;
        data[1]=tgy;
        data[2]=tgz;
        data[3]=time;
        data[4]=intensity;
        data[5]=range;
        this.fileNum=fileNum;
    }

    public MolaFSHyperPoint(DataInputStream stream) throws IOException
    {
        read(stream);
    }

    @Override
    public double getCoordinate(int i)
    {
        return data[i]; // i goes from 0 to 3 so the ordering of tgx,tgy,tgz,time in the beginning of data[] is crucial
    }

    @Override
    public double[] getData()
    {
        return data;
    }

    @Override
    public int getDimension()
    {
        return 4;
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException    // the hyperpoint only has 4 coordinates but we need to write all 6
    {
        for (int i=0; i<6; i++)
            data[i]=inputStream.readDouble();
        fileNum=inputStream.readInt();
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException
    {
        for (int i=0; i<6; i++)
            outputStream.writeDouble(data[i]);
        outputStream.writeInt(fileNum);
    }

    @Override
    public int compareTo(LidarPoint o)
    {
        return getTime().compareTo(o.getTime());
    }

    @Override
    public Vector3D getTargetPosition()
    {
        return new Vector3D(data[0],data[1],data[2]);
    }

    @Override
    public Vector3D getSourcePosition()
    {
        return Vector3D.ZERO;
    }

    @Override
    public Double getIntensityReceived()
    {
        return data[4];
    }

    @Override
    public Double getTime()
    {
        return data[3];
    }

    @Override
    public int getSizeInBytes()
    {
        return Double.BYTES*data.length+Integer.BYTES;
    }

    public int getFileNum()
    {
        return fileNum;
    }

    @Override
    public double getRange()
    {
        return data[5];
    }


}
