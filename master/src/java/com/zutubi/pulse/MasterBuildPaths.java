package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.io.File;

/**
 */
public class MasterBuildPaths
{
    private File rootBuildDir;

    public MasterBuildPaths(ConfigurationManager configManager)
    {
        rootBuildDir = configManager.getUserPaths().getProjectRoot();
    }

    public static String getProjectDirName(Project project)
    {
        return Long.toString(project.getId());
    }

    public File getProjectDir(Project project)
    {
        return new File(rootBuildDir, getProjectDirName(project));
    }

    public File getBuildsDir(Project project)
    {
        return new File(getProjectDir(project), "builds");
    }

    public static String getBuildDirName(BuildResult result)
    {
        return String.format("%08d", result.getNumber());
    }

    public File getBuildDir(Project project, BuildResult result)
    {
        return new File(getBuildsDir(project), getBuildDirName(result));
    }

    public File getRecipeDir(Project project, BuildResult result, long recipeId)
    {
        return new File(getBuildDir(project, result), Long.toString(recipeId));
    }

    public File getOutputDir(Project project, BuildResult result, long recipeId)
    {
        return new File(getRecipeDir(project, result, recipeId), "output");
    }

    public File getBaseDir(Project project, BuildResult result, long recipeId)
    {
        return new File(getRecipeDir(project, result, recipeId), "work");
    }
}
