package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;

/**
 */
public class CopyBootstrapper implements Bootstrapper
{
    private String url;
    private long recipeId;

    public CopyBootstrapper(String url, long recipeId)
    {
        this.url = url;
        this.recipeId = recipeId;
    }

    public void bootstrap(RecipePaths paths) throws BuildException
    {
        // copy the contents of the previous working directory to the local work path.

    }
}
