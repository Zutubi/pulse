package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.io.File;

/**
 * The Master Build Paths directory structure is as follows:
 *
 *  PROJECTS_ROOT/ -
 *      \-- (project-id)
 *          \--  repo
 *          \--  builds/
 *              \-- (build number)
 *                  \-- (recipe number)
 *                      \-- base
 *                      \-- output
 *
 */
public class MasterBuildPaths
{
    private File rootBuildDir;

    public MasterBuildPaths(MasterConfigurationManager configManager)
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

    public File getRepoDir(Project project)
    {
        return new File(getProjectDir(project), "repo");
    }

    public File getBuildsDir(Project project)
    {
        return new File(getProjectDir(project), "builds");
    }

    public static String getBuildDirName(BuildResult result)
    {
        return String.format("%08d", result.getNumber());
    }

    public File getBuildDir(BuildResult result)
    {
        return new File(getBuildsDir(result.getProject()), getBuildDirName(result));
    }

    public File getRecipeDir(BuildResult result, long recipeId)
    {
        return new File(getBuildDir(result), Long.toString(recipeId));
    }

    public File getOutputDir(BuildResult result, long recipeId)
    {
        return new File(getRecipeDir(result, recipeId), "output");
    }

    public File getBaseDir(BuildResult result, long recipeId)
    {
        return new File(getRecipeDir(result, recipeId), "base");
    }
}
