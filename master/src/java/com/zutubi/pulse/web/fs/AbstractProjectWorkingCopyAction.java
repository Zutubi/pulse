package com.zutubi.pulse.web.fs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.web.ActionSupport;

import java.io.File;

/**
 * <class-comment/>
 */
public class AbstractProjectWorkingCopyAction extends ActionSupport
{
    /**
     * The id of the build being examined.
     */
    private long buildId;

    /**
     * The path being being listed.  This path will be prefixed with the recipe id followed by the path
     * into the working copy.
     */
    private String path;

    private String recipeId = null;

    private String workingCopyPath = null;

    protected MasterConfigurationManager configurationManager;
    protected BuildManager buildManager;
    protected ProjectManager projectManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    protected String getRecipeId()
    {
        if (recipeId == null)
        {
            processPath();
        }
        return recipeId;
    }

    protected String getWorkingCopyPathSegment()
    {
        if (workingCopyPath == null)
        {
            processPath();
        }
        return workingCopyPath;
    }

    private void processPath()
    {
        if (!TextUtils.stringSet(path))
        {
            return;
        }

        // the first component of the path should be the recipe id.
        recipeId = path;
        workingCopyPath = "";

        int indexOfSep = path.indexOf(File.separator);
        if (indexOfSep != -1)
        {
            recipeId = path.substring(0, indexOfSep);
            workingCopyPath = path.substring(indexOfSep + 1);
        }
    }


    /**
     * Required resource.
     *
     * @param buildManager
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Required resource.
     *
     * @param projectManager
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

}
