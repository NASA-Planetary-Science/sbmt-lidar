package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface OctreePoint {
    public Vector3D getPosition();
    public void writeToStream(DataOutputStream stream) throws IOException;
    public void readFromStream(DataInputStream stream) throws IOException;
    public boolean isFullyRead();
}
