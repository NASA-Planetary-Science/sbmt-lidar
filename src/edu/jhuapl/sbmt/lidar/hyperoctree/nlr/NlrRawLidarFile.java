package edu.jhuapl.sbmt.lidar.hyperoctree.nlr;

import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.lidar.RawLidarFile;


public class NlrRawLidarFile extends RawLidarFile
{

    public enum Fields
    {
        longitudeE,
        latitudeN,
        Eros_rad,
        ET_J2000,
        UTC,
        Range,
        t,
        a,
        SClon,
        SClat,
        SCrdst,
        Emission,
        Offnadir,
        SCLCKCH,
        Eros_x,
        Eros_y,
        Eros_z,
        Omega,
        U;

    }

    public NlrRawLidarFile(String pathname)
    {
        super(pathname);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void readFully()
    {
        try
        {
            Scanner scanner=new Scanner(this);
            // skip header
            scanner.nextLine();
            scanner.nextLine();
            /* columns on file are
             * (1) longitudeE
             * (2) latitudeN
             * (3) Eros_rad
             * (4) ET_J2000
             * (5) UTC
             * (6) Range
             * (7) t
             * (8) a
             * (9) SClon
             * (10) SClat
             * (11) SCrdst
             * (12) Emission
             * (13) Offnadir
             * (14) SCLCKCH
             * (15) Eros_x
             * (16) Eros_y
             * (17) Eros_z
             * (18) Omega
             * (19) U
             */
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                String[] tokens=line.trim().split("[ \t]+");    // trim() whitespace on edges and split on any number of contiguous remaining spaces and tabs
                if (Integer.valueOf(tokens[Fields.a.ordinal()])==1) // noise
                    continue;
                double time=Double.valueOf(tokens[Fields.ET_J2000.ordinal()]);  // s
                double tgx=Double.valueOf(tokens[Fields.Eros_x.ordinal()]);     // m
                double tgy=Double.valueOf(tokens[Fields.Eros_y.ordinal()]);     // m
                double tgz=Double.valueOf(tokens[Fields.Eros_z.ordinal()]);     // m
                double sclat=Double.valueOf(tokens[Fields.SClat.ordinal()]);    // degrees
                double sclon=Double.valueOf(tokens[Fields.SClon.ordinal()]);    // degrees
                double scrad=Double.valueOf(tokens[Fields.SCrdst.ordinal()]);   // m
                double[] scxyz=MathUtil.latrec(new LatLon(Math.toRadians(sclat), Math.toRadians(sclon), scrad));
                Vector3D scpos=new Vector3D(scxyz).scalarMultiply(1e-3);        // km
                Vector3D tgpos=new Vector3D(tgx,tgy,tgz).scalarMultiply(1e-3);  // km
                double intensity=Double.valueOf(tokens[Fields.Emission.ordinal()]);
                points.add(new NlrLidarPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, scpos.getX(), scpos.getY(), scpos.getZ(), intensity, getFileNumber()));
            }
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int getFileNumber()
    {
        return getName().hashCode();
    }

}
