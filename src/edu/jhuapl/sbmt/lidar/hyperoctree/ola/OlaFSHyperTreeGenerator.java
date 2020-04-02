package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.misc.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.misc.RawLidarFile;


public class OlaFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public OlaFSHyperTreeGenerator(Path outputDirectory,
            int maxNumberOfPointsPerLeaf, HyperBox bbox,
            int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        super(outputDirectory, maxNumberOfPointsPerLeaf, bbox,
                maxNumberOfOpenOutputFiles, pool);
        // TODO Auto-generated constructor stub
    }

    @Override
    public RawLidarFile openFile(Path file)
    {
        return new OlaLidarFile(file.toString());
    }

}
