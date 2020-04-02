package edu.jhuapl.sbmt.lidar.misc;

import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.BasicLidarPoint;

public interface LidarPointList
{
    public int getNumberOfPoints();
    public BasicLidarPoint getPoint(int i);
    public void clear();
    public void appendFromFile(Path inputFilePath);
}
