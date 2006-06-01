package com.zutubi.pulse;

import com.zutubi.pulse.core.RecipePaths;

import java.io.File;

/**
 * The server recipe paths:
 * <p/>
 * system/recipes/xyz/work
 * /output
 * <p/>
 * where xyz is the recipe identifier.
 */
public class ServerRecipePaths implements RecipePaths
{
    private long id;
    private File dataDir;

    public ServerRecipePaths(long id, File dataDir)
    {
        this.id = id;
        this.dataDir = dataDir;
    }

    private File getRecipesRoot()
    {
        return new File(dataDir, "recipes");
    }

    public File getRecipeRoot()
    {
        return new File(getRecipesRoot(), Long.toString(id));
    }

    public File getBaseDir()
    {
        return new File(getRecipeRoot(), "base");
    }

    public File getOutputDir()
    {
        return new File(getRecipeRoot(), "output");
    }

    public File getBaseZip()
    {
        return new File(getBaseDir().getAbsolutePath() + ".zip");
    }

    public File getOutputZip()
    {
        return new File(getOutputDir().getAbsolutePath() + ".zip");
    }
}
