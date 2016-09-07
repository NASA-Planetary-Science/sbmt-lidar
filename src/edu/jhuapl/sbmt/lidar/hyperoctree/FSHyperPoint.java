package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface FSHyperPoint extends HyperPoint
{
    public void read(DataInputStream inputStream) throws IOException;
    public void write(DataOutputStream outputStream) throws IOException;
    public int getSizeInBytes();
}
