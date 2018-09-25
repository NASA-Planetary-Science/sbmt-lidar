package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeCondenser;

public class Hayabusa2LaserFSHyperTreeCondenser extends FSHyperTreeCondenser
{

    public Hayabusa2LaserFSHyperTreeCondenser(Path rootPath, Path outFilePath)
    {
        super(rootPath, outFilePath);
    }


    @Override
    public int getDimension()
    {
        return 5;
    }
}
