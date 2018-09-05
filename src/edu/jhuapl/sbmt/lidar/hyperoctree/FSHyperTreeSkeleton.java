package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.util.FileCache;

public class FSHyperTreeSkeleton
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

//        public boolean intersects(double[] bbox)
//        {
//            return bbox[0]<=bounds[1] && bbox[1]>=bounds[0] && bbox[2]<=bounds[3] && bbox[3]>=bounds[2] && bbox[4]<=bounds[5] && bbox[5]>=bounds[4] && bbox[6]<=bounds[7] && bbox[7]>=bounds[6];
//        }

        public Path getPath()
        {
            return path;
        }

        public double[] getBounds() {
            return bounds;
        }

        public Node[] getChildren()
        {
            return children;
        }
    }

    public FSHyperTreeSkeleton(Path dataSourcePath)  // data source path defines where the .lidar file representing the tree structure resides; basepath is its parent
    {
        this.dataSourcePath=dataSourcePath;
        this.basePath=dataSourcePath.getParent();
    }

    public double[] readBoundsFile(Path path)
    {
        File f=FileCache.getFileFromServer(path.toString());
        if (f.exists()) {
            return FSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);
        }

        f=FileCache.getFileFromServer(FileCache.FILE_PREFIX+path.toString());
        if (f.exists()) {
            return FSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);
        }

        return null;
    }

    public void read()  // cf. OlaFSHyperTreeCondenser for code to write the skeleton file
    {

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
        rootNode=new Node(rootBounds,basePath,false,idCount); // false -> root is not a leaf
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
                String[] tokens=line.split(" ");
                String num=tokens[0];
                String path=tokens[1];
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

    public void readChildren(Scanner scanner, Node node)
    {
        // default 4 dimensions: x, y, z, time
        readChildren(scanner, node, 4);
    }

    public void readChildren(Scanner scanner, Node node, int dimension)   // cf. OlaFSHyperTreeCondenser for code to write the skeleton
    {
        for (int i=0; i<(2^dimension); i++)
        {
            String line=scanner.nextLine();
            String[] tokens=line.replace("\n", "").replace("\r", "").split(" ");
            Path childPath=basePath.resolve(tokens[0]);
            String childInfo=tokens[1];
            //
            if (childInfo.equals("*"))   // child does not exist
                continue;
            //
            double[] bounds=new double[dimension*2];
            for (int j=0; j<(dimension*2); j++)
                bounds[j]=Double.valueOf(tokens[2+j]);
            //
            if(childInfo.equals(">"))  // child exists but is not a leaf (i.e. does not have data)
                node.children[i]=new Node(bounds, childPath, false, idCount);
            else if (childInfo.equals("d")) // child exists and is a leaf (i.e. does have data)
                node.children[i]=new Node(bounds, childPath, true, idCount);
            idCount++;
            nodeMap.put(node.children[i].id, node.children[i]);
        }
        for (int i=0; i<(2^dimension); i++)
            if (node.children[i]!=null && !node.children[i].isLeaf)
            {
                readChildren(scanner, node.children[i]);
            }
    }

    public Path getBasePath()
    {
        return basePath;
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(double[] searchBounds)
    {
        TreeSet<Integer> pathList=Sets.newTreeSet();
        try {
            getLeavesIntersectingBoundingBox(rootNode, searchBounds, pathList);
        } catch (HyperException e) {
            e.printStackTrace();
        }
        return pathList;
    }

    private void getLeavesIntersectingBoundingBox(Node node, double[] searchBounds, TreeSet<Integer> pathList) throws HyperException
    {
        // need to separate min and max bounds to create hyperbox
        double[] bounds = node.getBounds();
        int dim = bounds.length / 2;
        double[] min = new double[dim];
        double[] max = new double[dim];
        for(int ii = 0; ii < dim; ii++ ) {
            min[ii] = bounds[ii*2];
            max[ii] = bounds[ii*2 + 1];
        }
        HyperBox hbox_this = new HyperBox(min, max);

        // now create a hyperbox for search bounds
        dim = searchBounds.length / 2;
        min = new double[dim];
        max = new double[dim];
        for(int ii = 0; ii < dim; ii++ ) {
            min[ii] = searchBounds[ii*2];
            max[ii] = searchBounds[ii*2 + 1];
        }
        HyperBox hbox_search = new HyperBox(min, max);

        if (hbox_this.intersects(hbox_search) && node.isLeaf) {
            pathList.add(node.id);
        }
        for (int i=0; i<(2^dim); i++)
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

    public int getIdCount()
    {
        return idCount;
    }


}
