package edu.jhuapl.sbmt.lidar;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;

public class DataInputStreamPool
{
    Map<Path,DataInputStream> streamMap;
    int maxNumFiles;

//    static int cnt=0;

    public DataInputStreamPool(int maxNumFiles)
    {
        this.maxNumFiles=maxNumFiles;
        streamMap=Maps.newHashMapWithExpectedSize(maxNumFiles);
    }

    public DataInputStream getStream(Path path) throws IOException {
        DataInputStream stream=streamMap.get(path);
        return (stream==null)?newStream(path):stream;
    }

    DataInputStream newStream(Path path) throws IOException {
        if (streamMap.size()==maxNumFiles)
            closeStream(streamMap.keySet().iterator().next());    // remove a random entry in the map
        DataInputStream stream=new DataInputStream(new FileInputStream(path.toFile()));
        streamMap.put(path, stream);
        return stream;
    }

    public void closeStream(Path path) throws IOException {
        DataInputStream stream=streamMap.get(path);
        if (stream!=null) {
            stream.close();
            streamMap.remove(path);
        }
    }

    public void closeAllStreams() throws IOException {
        while (!streamMap.isEmpty())
            closeStream(streamMap.keySet().iterator().next());  // close random entries until empty
    }

    public int getNumberOfStreams() {
        return streamMap.size();
    }

    public boolean containsStream(DataInputStream stream)
    {
        return streamMap.values().contains(stream);
    }

/*    public static boolean endOfStream(DataInputStream stream)
    {
        try
        {
//            cnt++;
//            if ((cnt%100000)==0)
//                System.out.println(stream.available());
            return stream.available()==0;
        }
        catch (IOException e)
        {
            return true;
        }
    }*/

}
