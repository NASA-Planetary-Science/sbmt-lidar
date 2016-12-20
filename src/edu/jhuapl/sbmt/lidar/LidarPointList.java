package edu.jhuapl.sbmt.lidar;

import java.nio.file.Path;

public interface LidarPointList
{
    public int getNumberOfPoints();
    public BasicLidarPoint getPoint(int i);
    public void clear();
    public void appendFromFile(Path inputFilePath);
}
