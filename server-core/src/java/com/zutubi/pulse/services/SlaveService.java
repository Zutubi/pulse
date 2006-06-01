/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.services;

import com.zutubi.pulse.RecipeRequest;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.logging.CustomLogRecord;

import java.util.List;

/**
 */
public interface SlaveService
{
    /**
     * Do-nothing method just used to test communications.
     */
    void ping();

    boolean build(String master, RecipeRequest request);

    void cleanupRecipe(long recipeId);

    void terminateRecipe(long recipeId);

    SystemInfo getSystemInfo();

    List<CustomLogRecord> getRecentMessages();
}
