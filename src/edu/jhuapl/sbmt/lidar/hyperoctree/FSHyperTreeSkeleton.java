package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.util.FileCache;

public abstract class FSHyperTreeSkeleton
{

    Node rootNode;
    int idCount=0;
    TreeMap<Integer, Node> nodeMap=Maps.newTreeMap(); // unfortunately this extra level of indirection is required by the "LidarSearchDataCollection" class
    Path basePath;
    Path dataSourcePath;
    Map<Integer, String> fileMap=Maps.newHashMap();

    public class Node
    {
        double[] bounds;
        Path path;
        boolean isLeaf;
        Node[] children;
        int id;

        public Node(double[] bounds, Path path, boolean isLeaf, int id)
        {
            this.bounds=bounds;
            this.path=path;
            this.isLeaf=isLeaf;
            children=new Node[16];
            for (int i=0; i<16; i++)
                children[i]=null;
            this.id=id;
        }

        public boolean intersects(double[] bbox)
        {
            return bbox[0]<=bounds[1] && bbox[1]>=bounds[0] && bbox[2]<=bounds[3] && bbox[3]>=bounds[2] && bbox[4]<=bounds[5] && bbox[5]>=bounds[4] && bbox[6]<=bounds[7] && bbox[7]>=bounds[6];
        }

        public Path getPath()
        {
            return path;
        }
    }

    public FSHyperTreeSkeleton(Path dataSourcePath)  // data source path defines where the .lidar file representing the tree structure resides; basepath is its parent
    {
        this.dataSourcePath=dataSourcePath;
        this.basePath=dataSourcePath.getParent();
    }

    protected abstract double[] readBoundsFile(Path path);

    public void read()  // cf. OlaFSHyperTreeCondenser for code to write the skeleton file
    {
/*        File fp=FileCache.getFileFromServer(dataSourcePath.getParent().toString());
        if (!fp.exists())
            try
            {
                FileUtils.forceMkdir(fp);
            }
            catch (IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }*/

        File f=FileCache.getFileFromServer(dataSourcePath.toString());
        if (!f.exists())
        {
            try
            {
                FileUtils.forceMkdir(f.getParentFile());
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        f=FileCache.getFileFromServer(dataSourcePath.toString());
        if (!f.exists())
            f=FileCache.getFileFromServer(FileCache.FILE_PREFIX+dataSourcePath.toString());
        //
        double[] rootBounds=readBoundsFile(basePath.resolve("bounds"));
        rootNode=new Node(rootBounds,basePath,true,idCount); // false -> root is not a leaf
        nodeMap.put(rootNode.id, rootNode);
        idCount++;
        //
        try
        {
            Scanner scanner=new Scanner(f);
            readChildren(scanner, rootNode);
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //
        Path fileMapPath=dataSourcePath.getParent().resolve("fileMap.txt");
        f=FileCache.getFileFromServer(fileMapPath.toString());
        if (!f.exists())
            f=FileCache.getFileFromServer(FileCache.FILE_PREFIX+fileMapPath.toString());
        System.out.println("File map = "+f.toString());
        try
        {
            Scanner scanner=new Scanner(f);
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                String num=line.split(" ")[0];
                String path=line.replace(num, "").trim();
                fileMap.put(Integer.valueOf(num), path);
            }
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readChildren(Scanner scanner, Node node)   // cf. OlaFSHyperTreeCondenser for code to write the skeleton
    {
        node.isLeaf=true;
        for (int i=0; i<16; i++)
        {
            String line=scanner.nextLine();
            String[] tokens=line.replace("\n", "").replace("\r", "").split(" ");
            Path childPath=basePath.resolve(tokens[0]);
            String childInfo=tokens[1];
            //
            if (childInfo.equals("*"))   // child does not exist
                continue;
            //
            node.isLeaf=false;
            double[] bounds=new double[8];
            for (int j=0; j<8; j++)
                bounds[j]=Double.valueOf(tokens[2+j]);
            //
            if(childInfo.equals(">"))  // child exists but is not a leaf (i.e. does not have data)
                node.children[i]=new Node(bounds, childPath, false, idCount);
            else if (childInfo.equals("d")) // child exists and is a leaf (i.e. does have data)
                node.children[i]=new Node(bounds, childPath, true, idCount);
            idCount++;
            nodeMap.put(node.children[i].id, node.children[i]);
        }
        for (int i=0; i<16; i++)
            if (node.children[i]!=null && !node.children[i].isLeaf)
            {
                readChildren(scanner, node.children[i]);
            }
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(double[] searchBounds)
    {
        TreeSet<Integer> pathList=Sets.newTreeSet();
        getLeavesIntersectingBoundingBox(rootNode, searchBounds, pathList);
        return pathList;
    }

    private void getLeavesIntersectingBoundingBox(Node node, double[] searchBounds, TreeSet<Integer> pathList)
    {
        if (node.intersects(searchBounds) && node.isLeaf)
            pathList.add(node.id);
        for (int i=0; i<16; i++)
            if (node.children[i]!=null)
                getLeavesIntersectingBoundingBox(node.children[i],searchBounds,pathList);
    }

    public Node getNodeById(int id)
    {
        return nodeMap.get(id);
    }


    public Map<Integer, String> getFileMap()
    {
        return fileMap;
    }

}
