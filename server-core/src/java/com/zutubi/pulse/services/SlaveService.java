package com.zutubi.pulse.services;

import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.logging.CustomLogRecord;

import java.util.List;

/**
 */
public interface SlaveService
{
    /**
     * Most primitive communication, do *not* change the signature of this
     * method.
     *
     * @return the build number of the slave (we will only continue to talk
     *         if the build number matches ours)
     */
    int ping();

    boolean build(String master, long slaveId, RecipeRequest request);

    void cleanupRecipe(long recipeId);

    void terminateRecipe(long recipeId);

    SystemInfo getSystemInfo();

    List<CustomLogRecord> getRecentMessages();
}
