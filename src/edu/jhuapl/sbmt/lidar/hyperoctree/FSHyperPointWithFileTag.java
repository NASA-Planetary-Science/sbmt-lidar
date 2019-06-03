package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.sbmt.lidar.LidarPoint;


public class FSHyperPointWithFileTag implements FSHyperPoint, LidarPoint
{

    // there are 9 data values but only 5 are used to define the hyperspace: tgx,tgy,tgz,time, range
    protected double[] data=new double[9];    // tgx,tgy,tgz,time,scx,scy,scz, range,intensity
    protected int fileNum;

    public FSHyperPointWithFileTag()
    {
        // TODO Auto-generated constructor stub
    }

    public static FSHyperPointWithFileTag wrap(LidarPoint pt, int filenum)
    {
        return new FSHyperPointWithFileTag(pt.getTargetPosition().getX(),pt.getTargetPosition().getY(),pt.getTargetPosition().getZ(),pt.getTime(),
                pt.getSourcePosition().getX(),pt.getSourcePosition().getY(),pt.getSourcePosition().getZ(),pt.getRangeToSC(), pt.getIntensityReceived(),filenum);
    }

    public FSHyperPointWithFileTag(double tgx, double tgy, double tgz, double time, double scx, double scy, double scz, double range, double intensity, int fileNum)
    {
        data[0]=tgx;
        data[1]=tgy;
        data[2]=tgz;
        data[3]=time;
        data[4]=scx;
        data[5]=scy;
        data[6]=scz;
        data[7]=range;
        data[8]=intensity;
        this.fileNum=fileNum;
    }

    public FSHyperPointWithFileTag(DataInputStream stream) throws IOException
    {
        read(stream);
    }

    @Override
    public double getCoordinate(int i)
    {
        return data[i];
    }

    @Override
    public double[] get()
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
        for (int i=0; i<data.length; i++)
            data[i]=inputStream.readDouble();
        fileNum=inputStream.readInt();
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException
    {
        for (int i=0; i<data.length; i++)
            outputStream.writeDouble(data[i]);
        outputStream.writeInt(fileNum);
    }

    @Override
    public int compareTo(LidarPoint o)
    {
        return Double.compare(getTime(), o.getTime());
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
    public double getIntensityReceived()
    {
        return data[7];
    }

    @Override
    public double getTime()
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
    public double getRangeToSC()
    {
        return data[8];
    }


	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FSHyperPointWithFileTag)) {
			return false;
		}
		final FSHyperPointWithFileTag other = (FSHyperPointWithFileTag) obj;
		if (data != other.data) {
			return false;
		}
		if (fileNum != other.fileNum) {
			return false;
		}
		return true;
	}

}
