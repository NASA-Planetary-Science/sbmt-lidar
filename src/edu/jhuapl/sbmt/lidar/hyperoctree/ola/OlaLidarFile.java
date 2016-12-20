package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.lidar.RawLidarFile;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;

public class OlaLidarFile extends RawLidarFile
{

    public OlaLidarFile(String pathname)
    {
        super(pathname);
        // TODO Auto-generated constructor stub
    }

/*                    in.skipBytes(17 + 8 + 24);
                    time = FileUtil.readDoubleAndSwap(in);
                    in.skipBytes(8 + 2 * 3);
                    short flagStatus = MathUtil.swap(in.readShort());
                    noise = ((flagStatus == 0 || flagStatus == 1) ? false : true);
                    in.skipBytes(8 + 8 * 4);
                    x = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    y = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    z = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    tgpos=new Vector3D(x,y,z);
                    in.skipBytes(8 * 3);
                    x = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    y = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    z = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    scpos=new Vector3D(x,y,z);
*/
    enum Field
    {
        TIME(17+8+24,Double.class),
        STATUS(17+8+24+Double.BYTES+8+2*3,Short.class),
        TGPOS(17+8+24+Double.BYTES+8+2*3+Short.BYTES+8+8*4,Vector3D.class),
        SCPOS(17+8+24+Double.BYTES+8+2*3+Short.BYTES+8+8*4+3*Double.BYTES+8*3,Vector3D.class);

        int offset;
        Class<?> type;

        private Field(int offset, Class<?> type)
        {
            this.offset=offset;
            this.type=type;
        }

        public int getOffset()
        {
            return offset;
        }

        public Class<?> getType()
        {
            return type;
        }

    }

    static final int timeOffset=1+17+8+24;
    static final int statusOffset=timeOffset+Double.BYTES+8+2*3;
    static final int tgposOffset=statusOffset+Short.BYTES+8+8*4;
    static final int scposOffset=tgposOffset+3*Double.BYTES+8*3;
    static final int recordLength=scposOffset+3*Double.BYTES;

    class OlaRecord
    {
        double time;
        boolean isNoise;
        Vector3D tgpos,scpos;
        double intensity=0;

        public OlaRecord(byte[] recBuf)
        {
            ByteBuffer buf=ByteBuffer.wrap(recBuf);
            byte[] rawTime=new byte[Double.BYTES];
            buf.position(timeOffset);
            for (int i=0; i<rawTime.length; i++)
                rawTime[rawTime.length-1-i]=buf.get();
            time=ByteBuffer.wrap(rawTime).getDouble();
            //
            byte[] rawStatus=new byte[Short.BYTES];
            buf.position(statusOffset);
            for (int i=0; i<rawStatus.length; i++)
                rawStatus[rawStatus.length-1-i]=buf.get();
            short status=ByteBuffer.wrap(rawStatus).getShort();
            isNoise=((status == 0 || status == 1) ? false : true);
            //
            byte[] rawTgpos=new byte[Double.BYTES*3];
            buf.position(tgposOffset);
            for (int i=0; i<rawTgpos.length; i++)
                rawTgpos[rawTgpos.length-1-i]=buf.get();
            ByteBuffer tgposBuf=ByteBuffer.wrap(rawTgpos);
            tgpos=new Vector3D(tgposBuf.getDouble(),tgposBuf.getDouble(),tgposBuf.getDouble()).scalarMultiply(1e-3);
            //
            byte[] rawScpos=new byte[Double.BYTES*3];
            buf.position(scposOffset);
            for (int i=0; i<rawScpos.length; i++)
                rawScpos[rawScpos.length-1-i]=buf.get();
            ByteBuffer scposBuf=ByteBuffer.wrap(rawScpos);
            scpos=new Vector3D(scposBuf.getDouble(),scposBuf.getDouble(),scposBuf.getDouble()).scalarMultiply(1e-3);
        }

        public FSHyperPointWithFileTag getAsHyperPoint()
        {
            return new FSHyperPointWithFileTag(tgpos.getX(),tgpos.getY(),tgpos.getZ(),time,scpos.getX(),scpos.getY(),scpos.getZ(),intensity,getFileNumber());
        }
    }

    @Override
    protected void readFully()
    {
        DataInputStream in;
        try
        {
            String filePathString=this.toString();
            if (!filePathString.endsWith(".l2"))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected .l2");

            byte[] buf=new byte[recordLength];
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(this)));
            while (true)
            {
                try
                {
                    buf[0]=in.readByte();   // get the first byte to see if eof has been reached
                }
                catch(EOFException e)
                {
                    break;
                }
                in.readFully(buf,1,recordLength-1); // read the rest of the record
                OlaRecord rec=new OlaRecord(buf);
                if (!rec.isNoise)
                    points.add(rec.getAsHyperPoint());
            }

            in.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Deprecated
    protected void readFullyOld()
    {
        try
        {
            String filePathString=this.toString();
            if (!filePathString.endsWith(".l2"))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected .l2");

            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this)));

            while (true)
            {
                boolean noise = false;
                Vector3D scpos=null;
                Vector3D tgpos=null;
                double time=0;
                double intensity=0;
                double x,y,z;

                try
                {
                    in.readByte();
                }
                catch(EOFException e)
                {
                    break;
                }

                try
                {
                    in.skipBytes(17 + 8 + 24);
                    time = FileUtil.readDoubleAndSwap(in);
                    in.skipBytes(8 + 2 * 3);
                    short flagStatus = MathUtil.swap(in.readShort());
                    noise = ((flagStatus == 0 || flagStatus == 1) ? false : true);
                    in.skipBytes(8 + 8 * 4);
                    x = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    y = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    z = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    tgpos=new Vector3D(x,y,z);
                    in.skipBytes(8 * 3);
                    x = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    y = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    z = FileUtil.readDoubleAndSwap(in) / 1000.0;
                    scpos=new Vector3D(x,y,z);
                }
                catch(IOException e)
                {
                    in.close();
                    throw e;
                }

                if (!noise)
                {
                    points.add(new FSHyperPointWithFileTag(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), intensity, getFileNumber()));
                }
            }
            in.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getFileNumber()
    {
        return getName().hashCode();
    }

    public static void main(String[] args)
    {
        OlaLidarFile file=new OlaLidarFile("/Volumes/freeman 1.8TB/sbmt/OLA/browse/test/OBJLIST001.l2");
    }

}
