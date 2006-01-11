package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ChainBootstrapper implements Bootstrapper
{
    private List<Bootstrapper> bootstrappers = new LinkedList<Bootstrapper>();

    public ChainBootstrapper(Bootstrapper ...bootstrappers)
    {
        this.bootstrappers.addAll(Arrays.asList(bootstrappers));
    }

    public void bootstrap(long recipeId, RecipePaths paths) throws BuildException
    {
        for (Bootstrapper bootstrapper : bootstrappers)
        {
            bootstrapper.bootstrap(recipeId, paths);
        }
    }

    public ChainBootstrapper add(Bootstrapper bootstrapper)
    {
        bootstrappers.add(bootstrapper);
        return this;
    }
}
