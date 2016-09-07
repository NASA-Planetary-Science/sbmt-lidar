package edu.jhuapl.sbmt.lidar.test;

public interface LidarTrack extends LidarPointList
{
    public boolean isHidden();
    public void setHidden(boolean flag);
    public int[] getColor();
    public void setColor(int[] c);
}
