package edu.jhuapl.sbmt.lidar.hyperoctree.mola;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;

public class MolaFSHyperTreeSkeleton extends FSHyperTreeSkeleton
{


    public MolaFSHyperTreeSkeleton(Path dataSourcePath)
    {
        super(dataSourcePath);
    }

    protected double[] readBoundsFile(Path path)
    {
        File f=FileCache.getFileFromServer(path.toString());
        if (f.exists())
            return MolaFSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);
        //
        f=FileCache.getFileFromServer(FileCache.FILE_PREFIX+path.toString());
        if (f.exists())
            return MolaFSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);

        //
        return null;
    }

}
