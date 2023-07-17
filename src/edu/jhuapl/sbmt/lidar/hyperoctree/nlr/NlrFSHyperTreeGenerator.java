package edu.jhuapl.sbmt.lidar.hyperoctree.nlr;

import java.nio.file.Path;

import edu.jhuapl.sbmt.core.io.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.misc.RawLidarFile;

public class NlrFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public NlrFSHyperTreeGenerator(Path outputDirectory,
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
        return new NlrRawLidarFile(file.toString());
    }


}
