package com.zutubi.pulse.master.rest.model.fs;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a file in a hierarchical file system.
 */
public class FileModel
{
    private String name;
    private boolean directory;
    private List<FileModel> nested;

    public FileModel(String name, boolean directory)
    {
        this.name = name;
        this.directory = directory;
    }

    public String getName()
    {
        return name;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public List<FileModel> getNested()
    {
        return nested;
    }

    public void addNested(FileModel f)
    {
        if (nested == null)
        {
            nested = new ArrayList<>();
        }

        nested.add(f);
    }
}
