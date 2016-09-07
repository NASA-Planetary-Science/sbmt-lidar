package edu.jhuapl.sbmt.lidar.test;

public class OlaLidarTrack extends OlaPointList implements LidarTrack
{
    private static int[] defaultColor = {0, 0, 255, 255};

    boolean hidden=false;
    int[] color=defaultColor;

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    @Override
    public void setHidden(boolean flag)
    {
        hidden=flag;
    }

    @Override
    public int[] getColor()
    {
        return color;
    }

    @Override
    public void setColor(int[] c)
    {
        color=c.clone();
    }

}
