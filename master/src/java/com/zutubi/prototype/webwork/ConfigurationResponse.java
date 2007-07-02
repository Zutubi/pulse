package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.record.PathUtils;
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
     * New paths added by the action, along with details used for UI display.
     */
    private List<Addition> addedFiles;
    /**
     * Configuration paths removed by the action.
     */
    private List<String> removedPaths;
    /**
     * List of all paths renamed by the action.
     */
    private List<Rename> renamedPaths;
    /**
     * The new path to redirect to.
     */
    private String newPath;
    /**
     * The new template path to redirect to.
     */
    private String newTemplatePath;

    public ConfigurationResponse(String newPath, String newTemplatePath)
    {
        this.newPath = newPath;
        this.newTemplatePath = newTemplatePath;
    }

    public boolean getSuccess()
    {
        return true;
    }

    public String getNewPath()
    {
        return newPath;
    }

    public String getNewTemplatePath()
    {
        return newTemplatePath;
    }

    public void addAddedFile(Addition addition)
    {
        if(addedFiles == null)
        {
            addedFiles = new LinkedList<Addition>();
        }

        addedFiles.add(addition);
    }

    @JSON
    public Addition[] getAddedFiles()
    {
        if(addedFiles == null)
        {
            return null;
        }

        return addedFiles.toArray(new Addition[addedFiles.size()]);
    }

    public void addRenamedPath(Rename rename)
    {
        if(renamedPaths == null)
        {
            renamedPaths = new LinkedList<Rename>();
        }

        renamedPaths.add(rename);
    }

    @JSON
    public Rename[] getRenamedPaths()
    {
        if(renamedPaths == null)
        {
            return null;
        }

        return renamedPaths.toArray(new Rename[renamedPaths.size()]);
    }

    public void addRemovedPath(String path)
    {
        if(removedPaths == null)
        {
            removedPaths = new LinkedList<String>();
        }

        removedPaths.add(path);
    }

    @JSON
    public String[] getRemovedPaths()
    {
        if(removedPaths == null)
        {
            return null;
        }

        return removedPaths.toArray(new String[removedPaths.size()]);
    }

    public static class Addition
    {
        private String parentPath;
        private String parentTemplatePath;
        private String baseName;
        private String displayName;
        private boolean leaf;

        public Addition(String path, String displayName, String templatePath, boolean leaf)
        {
            this.parentPath = PathUtils.getParentPath(path);
            if (templatePath != null)
            {
                this.parentTemplatePath = PathUtils.getParentPath(templatePath);
            }
            this.baseName = PathUtils.getBaseName(path);
            this.displayName = displayName;
            this.leaf = leaf;
        }

        public String getParentPath()
        {
            return parentPath;
        }

        public String getParentTemplatePath()
        {
            return parentTemplatePath;
        }

        public String getBaseName()
        {
            return baseName;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public boolean isLeaf()
        {
            return leaf;
        }
    }

    public static class Rename
    {
        private String oldPath;
        private String newName;

        public Rename(String oldPath, String newPath)
        {
            this.oldPath = oldPath;
            this.newName = PathUtils.getBaseName(newPath);
        }

        public String getOldPath()
        {
            return oldPath;
        }

        public String getNewName()
        {
            return newName;
        }
    }
}
