package edu.jhuapl.sbmt.lidar.hyperoctree.ola.masher;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperPoint;

public class OlaFootprintMasher
{

    static { NativeLibraryLoader.loadVtkLibraries(); }

    public final static Path nearSdcRootPath=Paths.get("/project/nearsdc/data");

    public static enum DataSource
    {
        DEFAULT("GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar","GASKELL/RQ36_V3/OLA/browse/default/fileList.txt"),
        NOISE("GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar","GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");

        Path treeSource,browseSource;

        private DataSource(String treeSource, String browseSource)
        {
            this.treeSource=Paths.get(treeSource);
            this.browseSource=Paths.get(browseSource);
        }

        public Path getTree()
        {
            return treeSource;
        }

        public Path getBrowse()
        {
            return browseSource;
        }
    }

    DataSource source;
    CachelessOlaFSHyperTreeSkeleton skeleton;

    public OlaFootprintMasher(DataSource source)
    {
        this.source=source;
        skeleton=new CachelessOlaFSHyperTreeSkeleton(source.getTree());
        skeleton.read();
    }


    public List<OlaFSHyperPoint> read(int fileNum)
    {
        List<OlaFSHyperPoint> points=Lists.newArrayList();
        Path filePath=source.getTree().resolve(Paths.get(skeleton.getFileMap().get(fileNum)).getFileName());
        DataInputStream in=null;

        try
        {
            int recordLength=(17 + 8 + 24)+Double.BYTES+(8 + 2 * 3)+Short.BYTES+(8 + 8 * 4)+Double.BYTES*3+(8 * 3)+Double.BYTES*3;
            int timeAddx=(17 + 8 + 24);
            int flagStatusAddx=(17 + 8 + 24)+Double.BYTES+(8 + 2 * 3);
            int tgPosAddx=(17 + 8 + 24)+Double.BYTES+(8 + 2 * 3)+Short.BYTES+(8 + 8 * 4);
            int scPosAddx=(17 + 8 + 24)+Double.BYTES+(8 + 2 * 3)+Short.BYTES+(8 + 8 * 4)+Double.BYTES*3+(8 * 3);

            in=new DataInputStream(new BufferedInputStream(new FileInputStream(filePath.toString())));

            byte[] buffer=new byte[recordLength];
            while (true)
            {
                in.readFully(buffer);
                double time=ByteBuffer.wrap(buffer,timeAddx,Double.BYTES).getDouble();
                short flagStatus=ByteBuffer.wrap(buffer,flagStatusAddx,Short.BYTES).getShort();
                double[] tgPosArray=ByteBuffer.wrap(buffer,tgPosAddx,Double.BYTES*3).asDoubleBuffer().array();
                double[] scPosArray=ByteBuffer.wrap(buffer,scPosAddx,Double.BYTES*3).asDoubleBuffer().array();
                boolean noise=((flagStatus == 0 || flagStatus == 1) ? false : true);
                if (!noise)
                    points.add(new OlaFSHyperPoint(tgPosArray[0], tgPosArray[1], tgPosArray[2], time, scPosArray[0], scPosArray[1], scPosArray[2], 0, fileNum));
            }

            //in.close();

        }
        catch (IOException e)
        {
            if (!(e instanceof EOFException))
                e.printStackTrace();
        }

        return points;


/*            while (true)
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
                }
            }
            in.close();*/



    }


    public static void main(String[] args)
    {
        OlaFootprintMasher masher=new OlaFootprintMasher(DataSource.valueOf(args[0]));
        masher.read(masher.skeleton.getFileMap().keySet().iterator().next());
    }

}
