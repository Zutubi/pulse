package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a task required to delete a config instance (and clean up after it).
 */
public class CleanupTaskModel
{
    private String path;
    private String summary;
    private List<CleanupTaskModel> children;

    public CleanupTaskModel(String path, String summary)
    {
        this.path = path;
        this.summary = summary;
    }

    public String getPath()
    {
        return path;
    }

    public String getSummary()
    {
        return summary;
    }

    public List<CleanupTaskModel> getChildren()
    {
        return children;
    }

    public void addChild(CleanupTaskModel child)
    {
        if (children == null)
        {
            children = new ArrayList<>();
        }

        children.add(child);
    }
}
