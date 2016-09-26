package edu.jhuapl.sbmt.lidar.hyperoctree.ola.masher;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;

import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeNode;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperTreeSkeleton;

public class CachelessOlaFSHyperTreeSkeleton extends OlaFSHyperTreeSkeleton
{

    public CachelessOlaFSHyperTreeSkeleton(Path dataSourcePath)
    {
        super(dataSourcePath);
    }

    @Override
    public void read()
    {
        double[] rootBounds=readBoundsFile(basePath.resolve("bounds"));
        rootNode=new Node(rootBounds,basePath,true,idCount); // false -> root is not a leaf
        nodeMap.put(rootNode.getId(), rootNode);
        idCount++;
        //
        try
        {
            File f=dataSourcePath.toFile();
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
        File fileMapFile=fileMapPath.toFile();
        System.out.println("File map = "+fileMapPath);
        try
        {
            Scanner scanner=new Scanner(fileMapFile);
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

    @Override
    protected double[] readBoundsFile(Path path)
    {
        return OlaFSHyperTreeNode.readBoundsFile(path, 4);
    }

}
