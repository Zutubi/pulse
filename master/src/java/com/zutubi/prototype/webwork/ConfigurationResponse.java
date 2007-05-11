package com.zutubi.prototype.webwork;

import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to carry data about a configuration action back to JavaScript on the
 * client so that it can update a split-pane configuration view.  This is
 * just data which is serialised to JSON.
 */
public class ConfigurationResponse
{
    /**
     * Configuration paths invalidated by the action.  Requires the reloading
     * of tree nodes if they are currently expanded.
     */
    private List<String> invalidatedPaths;
    /**
     * The new path to redirect to.
     */
    private String newPath;

    public ConfigurationResponse(String newPath)
    {
        this.newPath = newPath;
    }

    public boolean getSuccess()
    {
        return true;
    }

    public String getNewPath()
    {
        return newPath;
    }

    public void addInvalidatedPath(String path)
    {
        if(invalidatedPaths == null)
        {
            invalidatedPaths = new LinkedList<String>();
        }

        invalidatedPaths.add(path);
    }

    @JSON
    public String[] getInvalidatedPaths()
    {
        if(invalidatedPaths == null)
        {
            return null;
        }

        return invalidatedPaths.toArray(new String[invalidatedPaths.size()]);
    }
}
