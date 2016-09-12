package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;

public class OlaFSHyperTreeSkeleton extends FSHyperTreeSkeleton
{

    public OlaFSHyperTreeSkeleton(Path dataSourcePath)
    {
        super(dataSourcePath);
        // TODO Auto-generated constructor stub
    }


    protected double[] readBoundsFile(Path path)
    {
        File f=FileCache.getFileFromServer(path.toString());
        if (f.exists())
            return OlaFSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);
        //
        f=FileCache.getFileFromServer(FileCache.FILE_PREFIX+path.toString());
        if (f.exists())
            return OlaFSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);

        //
        return null;
    }


}
