/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.services;

import com.zutubi.pulse.RecipeRequest;

/**
 */
public interface SlaveService
{
    /**
     * Do-nothing method just used to test communications.
     */
    void ping();

    void build(String master, RecipeRequest request);

    void cleanupRecipe(long recipeId);

    void terminateRecipe(long recipeId);
}
