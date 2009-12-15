package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

/**
 * Convenient specialisation of {@link LogFile} for accessing recipe logs.
 */
public class RecipeLogFile extends LogFile
{
    public static final String LOG_FILENAME = "recipe.log";

    /**
     * Creates a new {@link LogFile} for accessing a recipe log.
     *
     * @param build    build the recipe is part of
     * @param recipeId database id of the recipe result
     * @param paths    used to locate build directories
     */
    public RecipeLogFile(BuildResult build, long recipeId, MasterBuildPaths paths)
    {
        super(new File(paths.getRecipeDir(build, recipeId), LOG_FILENAME), build.getProject().getConfig().getOptions().isLogCompressionEnabled());
    }
}
