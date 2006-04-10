package com.zutubi.pulse.core;

/**
 */
public interface Bootstrapper
{
    /**
     * Called just before the recipe request containing this bootstrapper is
     * dispatched.
     */
    void prepare() throws BobException;

    /**
     */
    void bootstrap(long recipeId, RecipePaths paths) throws BuildException;

}
