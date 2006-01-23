package com.cinnamonbob;

import java.io.File;

/**
 */
public interface BuildService extends RemoteService
{
    void build(RecipeRequest request);

    void collectResults(long recipeId, File outputDest, File workDest);

    void cleanup(long recipeId);

    // get available resources..... so that we can check to see if the
    // build host requirements are fullfilled.

    String getHostName();
}
