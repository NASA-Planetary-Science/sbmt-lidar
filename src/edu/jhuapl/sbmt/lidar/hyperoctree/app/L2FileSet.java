package edu.jhuapl.sbmt.lidar.hyperoctree.app;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class L2FileSet
{
    String description;
    private List<String> files;

    public L2FileSet()
    {
        // TODO Auto-generated constructor stub
    }

    public void addFile(String file)
    {
        getFiles().add(file);
    }

    public void setDescription(String description)
    {
        this.description=description.replaceAll("\n", "").replaceAll("\r", ""); // remove any carriage returns; the description is meant to be one line
    }


    public void save(Path outputFile)
    {
        try
        {
            Writer writer = new FileWriter(outputFile.toFile());
            writer.write("Description: ");
            writer.write(description+"\n");
            writer.write("Number of items: "+getFiles().size()+"\n");
            for (int i=0; i<getFiles().size(); i++)
                writer.write(getFiles().get(i)+"\n");
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void load(Path inputFile)
    {   getFiles().clear();
        try
        {
            Scanner scanner=new Scanner(inputFile.toFile());
            scanner.nextLine(); // read "Description" header
            description=scanner.nextLine();
            String[] tok=scanner.nextLine().split(" ");
            int nFiles=Integer.valueOf(tok[tok.length-1]);
            for (int i=0; i<nFiles; i++)
                getFiles().add(scanner.nextLine().replace("\n", "").replace("\r", ""));
            scanner.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public List<String> getFiles()
    {
        return files;
    }

    public void setFiles(List<String> files)
    {
        this.files = files;
    }


}
