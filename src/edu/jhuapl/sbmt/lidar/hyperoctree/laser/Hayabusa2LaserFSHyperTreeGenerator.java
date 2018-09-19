package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.RawLidarFile;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;

public class Hayabusa2LaserFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public Hayabusa2LaserFSHyperTreeGenerator(Path outputDirectory,
            int maxNumberOfPointsPerLeaf, HyperBox bbox,
            int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        super(outputDirectory, maxNumberOfPointsPerLeaf, bbox,
                maxNumberOfOpenOutputFiles, pool);
    }

    @Override
    public RawLidarFile openFile(Path file)
    {
        return new Hayabusa2LaserRawLidarFile(file.toString());
    }


}
