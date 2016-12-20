package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;


public class OlaFSHyperTreeSkeleton extends FSHyperTreeSkeleton
{


    public OlaFSHyperTreeSkeleton(Path dataSourcePath)  // data source path defines where the .lidar file representing the tree structure resides; basepath is its parent
    {
        super(dataSourcePath);
    }


}
