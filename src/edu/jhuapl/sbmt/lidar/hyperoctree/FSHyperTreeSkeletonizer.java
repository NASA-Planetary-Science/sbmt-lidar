package edu.jhuapl.sbmt.lidar.hyperoctree;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Stopwatch;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;

public class FSHyperTreeSkeletonizer implements Dimensioned
{
    static { NativeLibraryLoader.loadVtkLibraries(); }

    Path rootNodePath;
    Path outFilePath;
    Stopwatch sw;
    FileWriter writer;

    public FSHyperTreeSkeletonizer(Path rootPath, Path outFilePath)
    {
        rootNodePath=rootPath;
        this.outFilePath=outFilePath;
    }

    public void makeThemBones()
    {
        sw=new Stopwatch();
        sw.start();
        try
        {
            writer=new FileWriter(outFilePath.toFile());
            condense(rootNodePath);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void condense(Path nodePath) throws IOException
    {
        if (sw.elapsedMillis()>4000)
        {
            System.out.println(nodePath);
            sw.reset();
            sw.start();
        }
        //
/*        int nChildrenWithoutBounds=0;
        for (int i=0; i<getNumberOfChildrenPerNode(); i++)
        {
            Path boundsPath=nodePath.resolve(String.valueOf(i)).resolve("bounds");
            boolean hasBounds=boundsPath.toFile().exists();
            if (!hasBounds)
                nChildrenWithoutBounds++;
        }
        if (nChildrenWithoutBounds==getNumberOfChildrenPerNode())
            return; // if no children exist then don't bother descending this branch any farther
        //*/
        boolean[] descend=new boolean[getNumberOfChildrenPerNode()];
        for (int i=0; i<getNumberOfChildrenPerNode(); i++)
        {
            descend[i]=false;
            // child paths
            Path childPath=nodePath.resolve(String.valueOf(i));
            Path boundsPath=childPath.resolve("bounds");
            Path dataPath=childPath.resolve("data");
            boolean hasBounds=boundsPath.toFile().exists();
            boolean hasData=dataPath.toFile().exists();
            //
            writer.write(rootNodePath.relativize(childPath)+" "); // child path
            if (hasBounds)  // child has bounds, so write them
            {
                double[] bounds=FSHyperTreeNode.readBoundsFile(boundsPath, getDimension());
                //writer.write(rootNodePath.relativize(nodePath.resolve(String.valueOf(i)))+" "); // child path
                if (hasData)
                    writer.write("d "); // child also has data, so denote that
                else
                {
                    for (int l=0; l<getNumberOfChildrenPerNode(); l++)
                        if (childPath.resolve(String.valueOf(l)).toFile().exists())
                        {
                            descend[i]=true;
                            break;
                        }
                    if (descend[i])
                        writer.write("> ");
                    else
                        writer.write("* ");

                }
                for (int j=0; j<bounds.length; j++)
                    writer.write(BigDecimal.valueOf(bounds[j]).toPlainString()+" ");
            }
            else
                writer.write("* "); // bounds don't even exist, so ignore
            writer.write("\n");
        }
        for (int i=0; i<getNumberOfChildrenPerNode(); i++)
            if (descend[i])
                condense(nodePath.resolve(String.valueOf(i)));

    }




/*    public void save(Path outFile)
    {
        try
        {
            save(writer,rootNode);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/

/*    private void save(FileWriter writer, SimpleFSNode node) throws IOException
    {
        if (node==null)
        {
            String str="null\n";
            writer.write(str);
            System.out.print(str);
            return;
        }
        else
        {
            String str=node.path.toString()+"\n";
            writer.write(str);
            System.out.print(str);
        }
        for (int i=0; i<getNumberOfChildrenPerNode(); i++)
            save(writer,node.children[i]);
    }*/

    @Override
    public int getDimension()
    {
        return 4;
    }

    public int getNumberOfChildrenPerNode()
    {
        return (int)Math.pow(2, getDimension());
    }

/*    class SimpleFSNode
    {
        Path path;
        SimpleFSNode[] children=new SimpleFSNode[getNumberOfChildrenPerNode()];

        public SimpleFSNode(Path path)
        {
            this.path=path;
        }
    }*/

    public static void main(String[] args)
    {
        Path rootPath=Paths.get(args[0]);
        Path outFilePath=rootPath.resolve("dataSource.lidar");
        System.out.println("Root path = "+rootPath);
        System.out.println("Output path = "+outFilePath);
        FSHyperTreeSkeletonizer skeltonizer=new FSHyperTreeSkeletonizer(rootPath,outFilePath);
        skeltonizer.makeThemBones();
        System.out.println("Wrote tree structure to "+outFilePath);
    }
}
