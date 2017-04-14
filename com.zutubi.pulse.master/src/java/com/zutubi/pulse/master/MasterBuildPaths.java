/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.FeaturePersister;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;

import java.io.File;

/**
 * The Master Build Paths directory structure is as follows:
 *
 *  PROJECTS_ROOT/ -
 *      \-- (project-id)
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

    public File getBuildsDir(long userId)
    {
        return new File(getUserDir(userId), "builds");
    }

    public static String getBuildDirName(BuildResult result)
    {
        return getBuildDirName(result.getNumber());
    }

    public static String getBuildDirName(long number)
    {
        return getPatchFileName(number);
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

    public File getFeaturesDir(BuildResult result, long recipeId)
    {
        return FeaturePersister.getFeaturesDirectory(getRecipeDir(result, recipeId));
    }

    public File getUserPatchDir(long userId)
    {
        return new File(getUserDir(userId), "patches");
    }

    public File getUserPatchFile(long userId, long number)
    {
        return new File(getUserPatchDir(userId), getPatchFileName(number));
    }

    public File getUserPatchPropertiesFile(long userId, long number)
    {
        return new File(getUserPatchDir(userId), getPatchFileName(number) + ".properties");
    }

    private static String getPatchFileName(long number)
    {
        return String.format("%08d", number);
    }
}
