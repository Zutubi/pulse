package com.zutubi.pulse.master;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.RemoteService;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;

import java.io.File;
import java.util.List;

/**
 */
public interface AgentService extends RemoteService
{
    int ping();
    SlaveStatus getStatus(String masterLocation);
    boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize);

    List<Resource> discoverResources();
    SystemInfo getSystemInfo();
    List<CustomLogRecord> getRecentMessages();

    AgentConfiguration getAgentConfig();

    /**
     * Returns true iff the service has the given version of the given
     * resource.
     *
     * @param requirement the resource requirement
     *
     * @return true iff this service has the give resource version
     */
    boolean hasResource(ResourceRequirement requirement);

    boolean build(RecipeRequest request);

    /**
     * Collects files produced by the recipe execution to the given local
     * directories.  Note that working copy collection is optional.
     *
     * @param project     name of the project
     * @param recipeId    id of the recipe to collect results for
     * @param incremental true for incremental builds
     * @param outputDest  local directory to receive the output files
     *                    (artifacts)
     * @param workDest    local directory to receive the working copy, or null
     *                    if the working copy should not be collected
     */
    void collectResults(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern, File outputDest, File workDest);

    void cleanup(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern);

    /**
     * Terminates the given recipe if it is still running.  This method may
     * only be called *after* receiving the recipe commenced event for the
     * recipe.
     *
     * @param recipeId the recipe to terminate
     */
    void terminateRecipe(long recipeId);

    String getHostName();

    void garbageCollect();
}
