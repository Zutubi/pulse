package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.Entity;
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
 *  USERS_ROOT/
 *     \-- (user-id)
 *         \-- builds
 *         \-- patches
 */
public class MasterBuildPaths
{
    private File projectRoot;
    private File userRoot;

    public MasterBuildPaths(MasterConfigurationManager configManager)
    {
        projectRoot = configManager.getUserPaths().getProjectRoot();
        userRoot = configManager.getUserPaths().getUserRoot();
    }

    public static String getEntityDirName(Entity entity)
    {
        return Long.toString(entity.getId());
    }

    public File getProjectDir(Project project)
    {
        return new File(projectRoot, getEntityDirName(project));
    }

    public File getUserDir(long userId)
    {
        return new File(userRoot, Long.toString(userId));
    }

    public File getRepoDir(Project project)
    {
        return new File(getProjectDir(project), "repo");
    }

    public File getBuildsDir(Project project)
    {
        return new File(getProjectDir(project), "builds");
    }

    public File getBuildsDir(long userId)
    {
        return new File(getUserDir(userId), "builds");
    }

    public static String getBuildDirName(BuildResult result)
    {
        return String.format("%08d", result.getNumber());
    }

    public File getBuildDir(BuildResult result)
    {
        File base;
        if(result.getUser() == null)
        {
            base = getProjectDir(result.getProject());
        }
        else
        {
            base = getUserDir(result.getUser().getId());
        }

        return new File(base, getBuildDirName(result));
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

    public File getUserPatchDir(long userId)
    {
        return new File(getUserDir(userId), "patches");
    }

    public File getUserPatchFile(long userId, long number)
    {
        return new File(getUserPatchDir(userId), String.format("%08d", number));
    }
}
