package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.model.BobFileSource;

/**
 * A request to execute a specific recipe.  Includes details about how to
 * bootstrap this step of the build (e.g. by SCM checkout, or by using a
 * working directory left by a previous recipe).
 */
public class RecipeRequest
{
    /**
     * The unique identifier for the execution of this recipe.
     */
    private long id;
    /**
     * Used to bootstrap the working directory.
     */
    private Bootstrapper bootstrapper;
    /**
     * Source used to retrieve the bob file.
     */
    private BobFileSource bobFileSource;
    /**
     * The name of the recipe to execute, or null to execute the default.
     */
    private String recipeName;


    public RecipeRequest(long id, BobFileSource bobFileSource, String recipeName)
    {
        this.id = id;
        this.bobFileSource = bobFileSource;
        this.recipeName = recipeName;
    }

    public long getId()
    {
        return id;
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }

    public BobFileSource getBobFileSource()
    {
        return bobFileSource;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    public void setBootstrapper(Bootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
    }
}
