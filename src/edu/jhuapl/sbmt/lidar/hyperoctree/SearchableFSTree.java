package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class SearchableFSTree implements Dimensioned
{
    SimpleFSNode rootNode;
    Scanner scanner;

    public void build(Path structureFile)
    {
        try
        {
            scanner=new Scanner(structureFile.toFile());
            // read root bounds (which have no associated relative dataFilePath)
            double[] rootBounds=new double[2*getDimension()];
            for (int i=0; i<2*getDimension(); i++)
                rootBounds[i]=scanner.nextDouble();
            scanner.nextInt();  // read whether root has a "data" file, which it should not, so ignore
            rootNode=new SimpleFSNode(rootBounds, null, 0);
            //
            build(rootNode);
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void build(SimpleFSNode node)
    {
            while (scanner.hasNext())
            {
                String idStr=scanner.next();
                System.out.println(idStr);
                String[] idTokens=idStr.split("/");
                int nextNodeId=Integer.valueOf(idTokens[idTokens.length-1]);
                int nextNodeLevel=idTokens.length;
                //
                double[] bounds=new double[2*getDimension()];
                for (int j=0; j<2*getDimension(); j++)
                    bounds[j]=scanner.nextDouble();
                boolean hasData=(scanner.nextInt()==1);
                String nextDataFilePath;
                if (hasData)
                    nextDataFilePath=Paths.get(idStr).resolve("data").toString();
                else
                    nextDataFilePath=null;
                //
                if (nextNodeLevel>node.level)   // this means we've found a child
                    node.children[nextNodeLevel]=new SimpleFSNode(bounds, nextDataFilePath, nextNodeLevel);
                else
                    ;
            }
    }

    class SimpleFSNode
    {
        double[] bounds;
        String dataFilePath;
        SimpleFSNode[] children;
        int level;

        public SimpleFSNode(double[] bounds, String dataFilePath, int level)
        {
            this.bounds=bounds;
            this.dataFilePath=dataFilePath;
            this.level=level;
        }

        void addChild(int i, double[] bounds, String childDataFilePath)
        {
            children[i]=new SimpleFSNode(bounds, childDataFilePath, level+1);
        }

        boolean isLeaf()
        {
            return dataFilePath==null;
        }

    }

    @Override
    public int getDimension()
    {
        return 4;
    }

    public int getNumberOfChildrenPerNode()
    {
        return (int)Math.pow(2, getDimension());
    }

    public static void main(String[] args)
    {
        SearchableFSTree tree=new SearchableFSTree();
        tree.build(Paths.get("/Users/zimmemi1/sbmt/fsTreeStructure.txt"));
    }

}
