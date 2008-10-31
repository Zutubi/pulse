package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.type.record.PathUtils;
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

    /**
     * Creates a response with a new path and optional template path to
     * select in the view.
     *
     * @param newPath         new config path to select (config tree)
     * @param newTemplatePath new template path to select (hierarchy tree),
     *                        may be null
     */
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

    /**
     * Registers the path that this response selects as an addition.  This is
     * a common case - something new is added and we want it to be selected
     * in the UI.  A special case of {@link #addAddedFile(Addition)}.
     *
     * @param configurationTemplateManager required manager
     * @param configurationSecurityManager required manager
     */
    public void registerNewPathAdded(ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        String displayName = ToveUtils.getDisplayName(newPath, configurationTemplateManager);
        String iconCls = ToveUtils.getIconCls(newPath, configurationTemplateManager);
        boolean leaf = ToveUtils.isLeaf(newPath, configurationTemplateManager, configurationSecurityManager);
        TemplateNode templateNode = configurationTemplateManager.getTemplateNode(newPath);
        boolean templateLeaf = !(templateNode != null && templateNode.getChildren().size() > 0);
        addAddedFile(new Addition(newPath, displayName, newTemplatePath, iconCls, leaf, templateLeaf));
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
        private String iconCls;
        private boolean leaf;
        private boolean templateLeaf;

        public Addition(String path, String displayName, String templatePath, String iconCls, boolean leaf, boolean templateLeaf)
        {
            this.parentPath = PathUtils.getParentPath(path);
            if (templatePath != null)
            {
                this.parentTemplatePath = PathUtils.getParentPath(templatePath);
            }
            this.baseName = PathUtils.getBaseName(path);
            this.displayName = displayName;
            this.iconCls = iconCls;
            this.leaf = leaf;
            this.templateLeaf = templateLeaf;
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

        public String getIconCls()
        {
            return iconCls;
        }

        public boolean isLeaf()
        {
            return leaf;
        }

        public boolean isTemplateLeaf()
        {
            return templateLeaf;
        }
    }

    public static class Rename
    {
        private String oldPath;
        private String newName;
        private String newDisplayName;

        public Rename(String oldPath, String newPath, String newDisplayName)
        {
            this.oldPath = oldPath;
            this.newName = PathUtils.getBaseName(newPath);
            this.newDisplayName = newDisplayName;
        }

        public String getOldPath()
        {
            return oldPath;
        }

        public String getNewName()
        {
            return newName;
        }

        public String getNewDisplayName()
        {
            return newDisplayName;
        }
    }
}
