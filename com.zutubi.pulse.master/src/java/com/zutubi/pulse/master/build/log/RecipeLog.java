package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.master.MasterBuildPaths;

import java.io.File;
import java.io.InputStream;

/**
 * Abstraction around the recipe log that provides:
 * <ul>
 *   <li>the ability to tail the file</li>
 *   <li>access to the full file</li>
 *   <li>compression of complete logs</li>
 * </ul>
 */
public class RecipeLog
{
    public static final String RECIPE_LOG = "recipe.log";
    public static final String EXTENSION_ZIP = ".zip";

    private File recipeDirectory;
    private long recipeId;

    public RecipeLog(long recipeId, MasterBuildPaths buildPaths)
    {
        this.recipeId = recipeId;
        
    }

    public InputStream getInputStream()
    {
        return null;
//        return new LogInputStream(new FileInp)
    }

}
