package com.cinnamonbob.core;

/**
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(long recipeId, RecipePaths paths) throws BuildException;
}
