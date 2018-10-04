package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;

public class Hayabusa2LidarHypertreeSkeleton extends FSHyperTreeSkeleton
{

    public Hayabusa2LidarHypertreeSkeleton(Path dataSourcePath)
    {
        super(dataSourcePath);
    }

    @Override
    protected double[] readBoundsFile(Path path)
    {
        File f=FileCache.getFileFromServer(path.toString());
        if (f.exists())
            return FSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 5);
        //
        f=FileCache.getFileFromServer(FileCache.FILE_PREFIX+path.toString());
        if (f.exists())
            return FSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 5);

        //
        return null;
    }

    public void readChildren(Scanner scanner, Node node)
    {
        // 5 dimensions: x, y, z, time, range to S/C
        readChildren(scanner, node, 5);
    }
}
