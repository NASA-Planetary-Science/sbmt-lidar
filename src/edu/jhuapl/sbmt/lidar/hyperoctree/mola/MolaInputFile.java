package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

public class MolaInputFile
{
    Path file;
    Scanner scanner;
    int fileNum;

    public MolaInputFile(Path file, int filenum)
    {
        this.fileNum=filenum;
        try
        {
            scanner=new Scanner(file.toFile());
            scanner.nextLine();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean hasNextLine()
    {
        return scanner.hasNextLine();
    }

    public MolaFSHyperPoint getNextLidarPoint()
    {
        String line=scanner.nextLine();
        String[] tokens=line.trim().split("\\s+");   // split on whitespace, cf. http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
        if (tokens.length==0)
            return null;
        System.out.println(Arrays.toString(tokens));
        double lon=Double.valueOf(tokens[0]);   // degrees
        double lat=Double.valueOf(tokens[1]);   // degrees
        double r=Double.valueOf(tokens[2])/1000;            // km
        double range=Double.valueOf(tokens[3])/1000;        // km
        double time=Double.valueOf(tokens[5]);
        double intensity=0;
        //
        double[] xyz = MathUtil.latrec(new LatLon(lat/180.*Math.PI, lon/180.*Math.PI, r));
        Vector3D tgpos=new Vector3D(xyz[0],xyz[1],xyz[2]);
        //

        System.out.println(range);

        return new MolaFSHyperPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ(), time, intensity, range, fileNum);

    }

}
