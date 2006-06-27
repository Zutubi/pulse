package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.filesystem.local.LocalFileSystem;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

/**
 */
public class BrowseProjectDirAction extends AbstractBrowseDirAction
{
    private long buildId;
    private long recipeId;
    private BuildResult buildResult;
    private ConfigurationManager configurationManager;
    private boolean foundBase = true;


    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public void setRecipeId(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public Project getProject()
    {
        if(buildResult != null)
        {
            return buildResult.getProject();
        }

        return null;
    }

    public boolean getFoundBase()
    {
        return foundBase;
    }

    public String execute() throws Exception
    {
        buildResult = getBuildManager().getBuildResult(buildId);
        if (buildResult == null)
        {
            addActionError("Unknown build [" + buildId + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(buildResult.getProject());

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        java.io.File baseDir = paths.getBaseDir(buildResult.getProject(), buildResult, recipeId);

        // First check if the build is complete and has a working directory
        // If not, we forward to the same page, which tells the user the bad
        // news.
        if (!buildResult.completed() || !baseDir.isDirectory())
        {
            foundBase = false;
            return "dir";
        }

        return super.execute(new LocalFileSystem(baseDir));
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
