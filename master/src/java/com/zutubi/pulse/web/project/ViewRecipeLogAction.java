package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 */
public class ViewRecipeLogAction extends ProjectActionSupport
{
    private long id;
    private long buildId;
    private BuildResult buildResult;
    private RecipeResultNode resultNode;
    private MasterConfigurationManager configurationManager;
    private InputStream inputStream;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public RecipeResultNode getResultNode()
    {
        return resultNode;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String execute() throws Exception
    {
        buildResult = getBuildManager().getBuildResult(buildId);
        if(buildResult == null)
        {
            addActionError("Unknown build [" + buildId + "]");
            return ERROR;
        }

        resultNode = buildResult.findResultNode(id);
        if(resultNode == null)
        {
            addActionError("Unknown stage [" + id + "]");
            return ERROR;
        }

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File recipeLog = new File(paths.getRecipeDir(buildResult, resultNode.getResult().getId()), RecipeResult.RECIPE_LOG);
        if(recipeLog.exists())
        {
            try
            {
                inputStream = new FileInputStream(recipeLog);
            }
            catch(IOException e)
            {
                addActionError("Unable to open recipe log '" + recipeLog.getAbsolutePath() + "': " + e.getMessage());
                return ERROR;
            }
        }
        else
        {
            addActionError("Recipe log '" + recipeLog.getAbsolutePath() + "' does not exist");
            return ERROR;
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
