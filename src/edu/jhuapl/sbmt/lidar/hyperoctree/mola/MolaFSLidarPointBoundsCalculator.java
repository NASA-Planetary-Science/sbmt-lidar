package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.File;
import java.util.Scanner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
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

            Scanner scanner=new Scanner(f);
            scanner.nextLine();
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                String[] tokens=line.trim().split("\\s+");   // split on whitespace, cf. http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
                if (tokens.length==0)
                    break;
                double lon=Double.valueOf(tokens[0]);   // degrees
                double lat=Double.valueOf(tokens[1]);   // degrees
                double r=Double.valueOf(tokens[2])/1000;            // convert to km
                double time=Double.valueOf(tokens[5]);
                double intensity=0;
                //
                double[] xyz = MathUtil.latrec(new LatLon(lat/180.*Math.PI, lon/180.*Math.PI, r));
                //
                Vector3D tgpos=new Vector3D(xyz[1],xyz[0],xyz[2]);
                System.out.println(tgpos);
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
