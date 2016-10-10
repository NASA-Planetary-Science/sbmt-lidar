package edu.jhuapl.sbmt.lidar.hyperoctree.ola.footprints;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import vtk.vtkBitArray;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkTriangle;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;


public class OlaFootprintReader
{
    static { NativeLibraryLoader.loadVtkLibraries(); }

    vtkPolyData polyData;

    public OlaFootprintReader(Path footprintVtkFile)
    {
        vtkPolyDataReader reader=new vtkPolyDataReader();
        reader.SetFileName(footprintVtkFile.toString());
        reader.Update();

        polyData=new vtkPolyData();
        polyData.DeepCopy(reader.GetOutput());
    }

    public BiMap<Integer, String> getAllL2FileNames()
    {
        BiMap<Integer, String> fileNames=HashBiMap.create();
        for (int i=0; i<polyData.GetCellData().GetNumberOfArrays(); i++)
            fileNames.put(i, polyData.GetCellData().GetArrayName(i));
        return fileNames;
    }

    public Set<Integer> getAllFacesInFootprint(int footprintNumber)
    {
        Set<Integer> faceIds=Sets.newHashSet();
        vtkBitArray flag=(vtkBitArray)polyData.GetCellData().GetArray(footprintNumber);
        for (int i=0; i<flag.GetNumberOfTuples(); i++)
            if (flag.GetValue(i)==1)
                faceIds.add(i);
        return faceIds;
    }

    public Set<Integer> getAllFootprintsOverlappingFootprint(int footprintNumber)
    {
        Set<Integer> footprintIds=Sets.newHashSet();
        Set<Integer> footprintFaceIds=getAllFacesInFootprint(footprintNumber);
        for (int i=0; i<polyData.GetCellData().GetNumberOfArrays(); i++)
        {
            vtkBitArray flag=(vtkBitArray)polyData.GetCellData().GetArray(i);
            boolean overlapping=false;
            for (int j=0; j<flag.GetNumberOfTuples() && !overlapping; j++)
            {
                if (flag.GetValue(j)==1 && footprintFaceIds.contains(j))    // if the candidate footprint contains even one face in the target footprint then add candidate to list
                {
                    footprintIds.add(i);
                    overlapping=true;
                }
            }
        }
        return footprintIds;
    }

    public vtkPolyData extractRawGeometry(Set<Integer> faceIds)
    {
        vtkPoints points=new vtkPoints();
        vtkCellArray cells=new vtkCellArray();
        for (int i=0; i<polyData.GetNumberOfCells(); i++)
            if (faceIds.contains(i))
            {
                vtkTriangle tri=new vtkTriangle();
                for (int j=0; j<3; j++)
                {
                    int id=points.InsertNextPoint(polyData.GetCell(i).GetPoints().GetPoint(j));
                    tri.GetPointIds().SetId(j, id);
                }
                cells.InsertNextCell(tri);
            }

        vtkPolyData polyDataSubset=new vtkPolyData();
        polyDataSubset.SetPoints(points);
        polyDataSubset.SetPolys(cells);
        return polyDataSubset;
    }

    public static void main(String[] args)
    {
        String fileName="testFootprints.vtk";
        System.out.println("Loading footprints from "+fileName+"...");
        OlaFootprintReader reader=new OlaFootprintReader(Paths.get(fileName));

        System.out.println("Footprints available:");
        BiMap<Integer, String> fileNames=reader.getAllL2FileNames();
        for (int i=0; i<fileNames.size(); i++)
            System.out.println(i+" "+fileNames.get(i));

        Set<Integer> footprintsOverlappingFirstOne=reader.getAllFootprintsOverlappingFootprint(0);
        Set<Integer> facesInMacroFootprint=Sets.newHashSet();
        for (int i : footprintsOverlappingFirstOne)
            facesInMacroFootprint.addAll(reader.getAllFacesInFootprint(i));
        vtkPolyData polyData=reader.extractRawGeometry(facesInMacroFootprint);

        String outputFile="macroFootprintTest.vtk";
        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetFileName(outputFile);
        writer.SetFileTypeToBinary();
        writer.SetInputData(polyData);
        writer.Write();

    }
}
