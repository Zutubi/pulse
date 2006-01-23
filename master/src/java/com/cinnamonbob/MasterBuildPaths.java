package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

import java.io.File;

/**
 */
public class MasterBuildPaths
{
    public MasterBuildPaths()
    {

    }

    public static String getProjectDirName(Project project)
    {
        return Long.toString(project.getId());
    }

    public File getProjectDir(Project project)
    {
        File rootBuildDir = ConfigUtils.getManager().getAppConfig().getProjectRoot();
        return new File(rootBuildDir, getProjectDirName(project));
    }

    public File getBuildsDir(Project project)
    {
        return new File(getProjectDir(project), "builds");
    }

    public static String getBuildDirName(BuildResult result)
    {
        return String.format("%08d", Long.valueOf(result.getNumber()));
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

    public File getWorkDir(Project project, BuildResult result, long recipeId)
    {
        return new File(getRecipeDir(project, result, recipeId), "work");
    }
}
