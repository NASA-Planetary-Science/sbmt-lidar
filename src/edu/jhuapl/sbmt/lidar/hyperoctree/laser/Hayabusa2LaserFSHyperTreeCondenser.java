package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeCondenser;

public class Hayabusa2LaserFSHyperTreeCondenser extends FSHyperTreeCondenser
{

    public Hayabusa2LaserFSHyperTreeCondenser(Path rootPath, Path outFilePath)
    {
        super(rootPath, outFilePath);
    }

    public static void main(String[] args)
    {
        // assumes this is being run from misc/scripts/generate_spectra_hypertree.sh
        // so the temp_hypertree/ directory has been created in working directory.  TODO
        Path rootPath=Paths.get(args[0]);
        Path outFilePath=rootPath.resolve("dataSource.lidar");
        System.out.println("Root path = "+rootPath);
        System.out.println("Output path = "+outFilePath);
        Hayabusa2LaserFSHyperTreeCondenser condenser=new Hayabusa2LaserFSHyperTreeCondenser(rootPath,outFilePath);
        condenser.condense();
        System.out.println("Wrote tree structure to "+outFilePath);
    }

    @Override
    public int getDimension()
    {
        return 5;
    }
}
