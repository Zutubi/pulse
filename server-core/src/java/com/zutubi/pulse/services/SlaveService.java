package com.zutubi.pulse.services;

import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.Resource;
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

    SlaveStatus getStatus(String token);

    /**
     * A request to build a recipe on the slave, if the slave is currently idle.
     *
     * @param token   secure token for inter-agent communication
     * @param master  location of the master for return messages
     * @param slaveId id of the slave, used in returned messages
     * @param request details of the recipe to build
     * @return true if the request was accepted, false of the slave was busy
     *
     * @throws InvalidTokenException if the given token does not match the
     * slave's
     */
    boolean build(String token, String master, long slaveId, RecipeRequest request) throws InvalidTokenException;

    void cleanupRecipe(String token, long recipeId) throws InvalidTokenException;

    void terminateRecipe(String token, long recipeId) throws InvalidTokenException;

    SystemInfo getSystemInfo(String token) throws InvalidTokenException;

    List<CustomLogRecord> getRecentMessages(String token) throws InvalidTokenException;

    List<Resource> discoverResources(String token);
}
