package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.RawLidarFile;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;

public class LaserFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public LaserFSHyperTreeGenerator(Path outputDirectory,
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
        return new LaserRawLidarFile(file.toString());
    }


}
