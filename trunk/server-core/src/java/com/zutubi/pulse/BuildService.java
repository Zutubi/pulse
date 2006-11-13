package com.zutubi.pulse;

import com.zutubi.pulse.core.RecipeRequest;

import java.io.File;

/**
 */
public interface BuildService extends RemoteService
{
    /**
     * Returns true iff the service has the given version of the given
     * resource.
     *
     * @param resource the name of the required resource
     * @param version  the required version, or null if no specific version
     *                 is required
     * @return true iff this service has the give resource version
     */
    boolean hasResource(String resource, String version);

    boolean build(RecipeRequest request, BuildContext context);

    /**
     * Collects files produced by the recipe execution to the given local
     * directories.  Note that working copy collection is optional.
     *
     * @param recipeId   id of the recipe to collect results for
     * @param outputDest local directory to receive the output files
     *                   (artifacts)
     * @param workDest   local directory to receive the working copy, or null
     *                   if the working copy should not be collected
     */
    void collectResults(String project, String spec, long recipeId, boolean incremental, File outputDest, File workDest);

    void cleanup(String project, String spec, long recipeId, boolean incremental);

    /**
     * Terminates the given recipe if it is still running.  This method may
     * only be called *after* receiving the recipe commenced event for the
     * recipe.
     *
     * @param recipeId the recipe to terminate
     */
    void terminateRecipe(long recipeId);

    // get available resources..... so that we can check to see if the
    // build host requirements are fullfilled.

    String getHostName();

}
