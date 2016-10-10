package edu.jhuapl.sbmt.lidar.hyperoctree.ola.footprints;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;

import vtk.vtkBitArray;
import vtk.vtkCellLocator;
import vtk.vtkIdList;
import vtk.vtkOBJReader;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.hyperoctree.ola.OlaFSHyperPoint;

public class OlaFootprintGenerator
{
    static { NativeLibraryLoader.loadVtkLibrariesHeadless(); }

    public static void main(String[] args)
    {
        // args[0] = directory where .l2 files reside
        // args[1] = mesh file to map points onto
        // args[2] = output file name

        Path browseDir=Paths.get(args[0]);
        System.out.println("Searching "+browseDir+" for .l2 files.");
        String[] l2Files=browseDir.toFile().list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".l2");
            }
        });
        System.out.println("Found files:");
        for (String f : l2Files)
            System.out.println("  "+f);

        vtkOBJReader reader=new vtkOBJReader();
        reader.SetFileName(args[1]);
        reader.Update();

        vtkPolyData polyData=new vtkPolyData();
        polyData.DeepCopy(reader.GetOutput());

        vtkCellLocator cellLocator=new vtkCellLocator();
        cellLocator.SetDataSet(polyData);
        cellLocator.SetTolerance(1e-12);
        cellLocator.BuildLocator();

        System.out.println();
        System.out.println("Processing footprints...");

        boolean[][] l2flag=new boolean[l2Files.length][polyData.GetNumberOfCells()];
        for (int m=0; m<l2Files.length; m++)
            for (int n=0; n<polyData.GetNumberOfCells(); n++)
                l2flag[m][n]=false;

        for (int i=0; i<l2Files.length; i++)
        {
            Stopwatch totalSw=new Stopwatch();
            totalSw.start();

            Path l2FilePath=browseDir.resolve(l2Files[i]);
            System.out.print("  Reading "+i+"/"+l2Files.length+" ("+l2FilePath+")...");
            OLAL2File l2File=new OLAL2File(l2FilePath);
            List<OlaFSHyperPoint> lidarPoints=l2File.read();

            Stopwatch sw=new Stopwatch();

            System.out.print("  Processing...");
            sw.start();

            //
            vtkIdList ids=new vtkIdList();
            for (int m=0; m<lidarPoints.size(); m++)
            {
                ids.Initialize();
                double[] pos=lidarPoints.get(m).getTargetPosition().toArray();
                cellLocator.FindCellsAlongLine(pos, Vector3D.ZERO.toArray(), 1e-12, ids);  // find closest point on target sphere
                if (ids.GetNumberOfIds()==0)
                    cellLocator.FindCellsAlongLine(pos, new Vector3D(pos).scalarMultiply(2).toArray(), 1e-12, ids);  // find closest point on target sphere
                if (ids.GetNumberOfIds()==0)
                    continue;
                int id=ids.GetId(0);
                l2flag[i][id]=true;
            }


            vtkBitArray flag=new vtkBitArray();
            flag.SetName(l2FilePath.toString());
            for (int m=0; m<polyData.GetNumberOfCells(); m++)
                flag.InsertNextValue(l2flag[i][m]?1:0);
            polyData.GetCellData().AddArray(flag);

            System.out.println(" "+sw.elapsedTime(TimeUnit.SECONDS)+"s");
            sw.reset();
        }

        String outFileName=args[2];
        System.out.print("  Writing to "+outFileName+"...");

        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetFileName(outFileName);
        writer.SetFileTypeToBinary();
        writer.SetInputData(polyData);
        writer.Write();
        System.out.println(" Done.");

    }
}
