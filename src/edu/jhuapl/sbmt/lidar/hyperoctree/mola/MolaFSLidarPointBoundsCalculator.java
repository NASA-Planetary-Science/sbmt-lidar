package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.File;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSLidarPointBoundsCalculator;

public class MolaFSLidarPointBoundsCalculator extends FSLidarPointBoundsCalculator
{
    static final String molaFileExtension="1.75";

    public MolaFSLidarPointBoundsCalculator(String inputDirectoryList)
    {
        super(inputDirectoryList,molaFileExtension);
    }

    public void checkBounds(File f)
    {
        try
        {
            String filePathString=f.toString();
            if (!filePathString.endsWith("."+molaFileExtension))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected "+molaFileExtension);



                MolaInputFile file=new MolaInputFile(f.toPath(), 0);
                while (file.hasNextLine())
                {
                    MolaFSHyperPoint point=file.getNextLidarPoint();
                    if (point==null)
                        break;
                    double time=point.getTime();
                    double tgx=point.getTargetPosition().getX();
                    double tgy=point.getTargetPosition().getY();
                    double tgz=point.getTargetPosition().getZ();
                    if (time>tmax)
                        tmax=time;
                    if (time<tmin)
                        tmin=time;
                    if (tgx>xmax)
                        xmax=tgx;
                    if (tgx<xmin)
                        xmin=tgx;
                    if (tgy>ymax)
                        ymax=tgy;
                    if (tgy<ymin)
                        ymin=tgy;
                    if (tgz>zmax)
                        zmax=tgz;
                    if (tgz<zmin)
                        zmin=tgz;
                }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

/*        try
        {
            String filePathString=f.toString();
            if (!filePathString.endsWith(".l2"))
                throw new Exception("Incorrect file extension \""+filePathString.substring(filePathString.lastIndexOf('.'))+"\" expected .l2");

            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));

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
                    if (time>tmax)
                        tmax=time;
                    if (time<tmin)
                        tmin=time;
                    double tgx=tgpos.getX();
                    double tgy=tgpos.getY();
                    double tgz=tgpos.getZ();
                    if (tgx>xmax)
                        xmax=tgx;
                    if (tgx<xmin)
                        xmin=tgx;
                    if (tgy>ymax)
                        ymax=tgy;
                    if (tgy<ymin)
                        ymin=tgy;
                    if (tgz>zmax)
                        zmax=tgz;
                    if (tgz<zmin)
                        zmin=tgz;
                }
            }
            in.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

}
