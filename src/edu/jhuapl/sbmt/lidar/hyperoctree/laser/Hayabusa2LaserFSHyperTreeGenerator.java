package edu.jhuapl.sbmt.lidar.hyperoctree.laser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.RawLidarFile;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;

public class Hayabusa2LaserFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public Hayabusa2LaserFSHyperTreeGenerator(Path outputDirectory,
            int maxNumberOfPointsPerLeaf, HyperBox bbox,
            int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        super(outputDirectory, maxNumberOfPointsPerLeaf, bbox,
                maxNumberOfOpenOutputFiles, pool);
        System.out.println("root bbox dims = "  + bbox.getDimension());
        setRoot(new Hayabusa2LaserHypertreeNode(null, outputDirectory, bbox, maxNumberOfPointsPerLeaf,pool));
    }

    @Override
    public RawLidarFile openFile(Path file)
    {
        return new Hayabusa2LaserRawLidarFile(file.toString());
    }

    @Override
    public void addAllPointsFromFile(Path inputPath) throws HyperException, IOException
    {
        RawLidarFile file=openFile(inputPath);
        file = (Hayabusa2LaserRawLidarFile)file;
        getFileMap().put(inputPath.getFileName(),file.getFileNumber());
        Iterator<LidarPoint> iterator=file.iterator();
        while (iterator.hasNext())
        {
            LidarPoint pt = iterator.next();
            getRoot().add(Hayabusa2LaserLidarPoint.wrap(pt, file.getFileNumber()));
            setTotalPointsWritten(getTotalPointsWritten() + 1);
        }
    }



}
