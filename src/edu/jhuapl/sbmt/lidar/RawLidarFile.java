package edu.jhuapl.sbmt.lidar;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class RawLidarFile extends File implements Iterable<LidarPoint>
{

    protected List<LidarPoint> points=Lists.newArrayList();

    public RawLidarFile(String pathname)
    {
        super(pathname);
        readFully();
    }

    protected abstract void readFully();
    public abstract int getFileNumber();

    @Override
    public Iterator<LidarPoint> iterator()
    {
        return points.iterator();
    }



}
