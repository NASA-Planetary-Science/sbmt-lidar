package edu.jhuapl.sbmt.lidar;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperPointWithFileTag;

public class OLAL2File
{
    Path path;
    int id; // id represents a file number that can be accessed later from each OLA point

    public OLAL2File(Path path)
    {
        this.path=path;
        this.id=-1;
    }

    public OLAL2File(Path path, int id)
    {
        this.path=path;
        this.id=id;
    }

    public List<FSHyperPointWithFileTag> read()
    {
        return read(1);
    }

    public List<FSHyperPointWithFileTag> read(double scale) // the fileNum field of the OLA point is set to -1 here
    {
        List<FSHyperPointWithFileTag> points= new ArrayList<>();
        DataInputStream in=null;

        try
        {
            int timeAddx=(1 + 17 + 8 + 24);
            int flagStatusAddx=timeAddx+Double.BYTES+(8 + 2 * 3);
            int intensityAddx=flagStatusAddx+Short.BYTES+(8 + 8 * 3);
            int tgPosAddx=flagStatusAddx+Short.BYTES+(8 + 8 * 4);
            int scPosAddx=tgPosAddx+Double.BYTES*3+(8 * 3);
            int recordLength=scPosAddx+Double.BYTES*3;

            in=new DataInputStream(new BufferedInputStream(new FileInputStream(path.toString())));

            byte[] readBuffer=new byte[recordLength];
            while (true)
            {
                double intensityReceived = 0;
                double rangeToSC = 0; // TODO range to s/c

                in.readFully(readBuffer);
                double time=ByteBuffer.wrap(readBuffer,timeAddx,Double.BYTES).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                short flagStatus=ByteBuffer.wrap(readBuffer,flagStatusAddx,Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
                intensityReceived = ByteBuffer.wrap(readBuffer,intensityAddx,Double.BYTES).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                double[] tgPos=new double[3];
                double[] scPos=new double[3];
                ByteBuffer.wrap(readBuffer,tgPosAddx,Double.BYTES*3).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().get(tgPos);
                ByteBuffer.wrap(readBuffer,scPosAddx,Double.BYTES*3).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().get(scPos);
//                System.out.println(time+" "+flagStatus+new Vector3D(tgPos)+new Vector3D(tgPos));
                //
                boolean noise = ((flagStatus == 0 || flagStatus == 1) ? false : true);
                if (!noise)
                    points.add(new FSHyperPointWithFileTag(tgPos[0]*scale, tgPos[1]*scale, tgPos[2]*scale, time, scPos[0]*scale, scPos[1]*scale, scPos[2]*scale, rangeToSC, intensityReceived, id));
            }

        }
        catch (IOException e)
        {

            if (!(e instanceof EOFException))
                e.printStackTrace();
        }


        if (in!=null)
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        return points;
    }

}
