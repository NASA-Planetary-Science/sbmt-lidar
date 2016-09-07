package edu.jhuapl.sbmt.lidar.test;

import java.nio.file.Path;
import java.util.Map;

public interface LidarSearch
{
    public double getOffsetScale();
    public Map<String, String> getDataSourceMap();
    public Path getDataSource();
}
