package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.sbmt.lidar.test.LidarPoint;

public class FSLidarHyperPoint implements FSHyperPoint, LidarPoint
{

    // there are 8 data values but only 4 are used to define the hyperspace: tgx,tgy,tgz,time
    double[] data=new double[8];    // tgx,tgy,tgz,time,scx,scy,scz,intensity
    int fileNum;

    public FSLidarHyperPoint()
    {
        // TODO Auto-generated constructor stub
    }

    public FSLidarHyperPoint(double tgx, double tgy, double tgz, double time, double scx, double scy, double scz, double intensity, int fileNum)
    {
        data[0]=tgx;
        data[1]=tgy;
        data[2]=tgz;
        data[3]=time;
        data[4]=scx;
        data[5]=scy;
        data[6]=scz;
        data[7]=intensity;
        this.fileNum=fileNum;
    }

    public FSLidarHyperPoint(DataInputStream stream) throws IOException
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
    public void read(DataInputStream inputStream) throws IOException    // the hyperpoint only has 4 coordinates but we need to write all 8
    {
        for (int i=0; i<8; i++)
            data[i]=inputStream.readDouble();
        fileNum=inputStream.readInt();
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException
    {
        for (int i=0; i<8; i++)
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
        return new Vector3D(data[4],data[5],data[6]);
    }

    @Override
    public Double getIntensityReceived()
    {
        return data[7];
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

}
