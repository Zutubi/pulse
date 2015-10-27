package com.zutubi.pulse.servercore.services;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerInfoModel;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;

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

    /**
     * The update mechanism needs to be stable.  Any changes to the way this
     * works requires knowledge in new code (master and slave side) that
     * knows to veto impossible updates.
     *
     * @param token      secure token for inter-agent communication
     * @param build      the build number to update to
     * @param master     url of the master requesting the update
     * @param hostId     the slave host's id, for when it calls us back
     * @param packageUrl URL from which a zip containing the given build can
     *                   be obtained
     * @param packageSize size, in bytes, of the package
     * @return true if the agent wishes to proceed with the update
     */
    boolean updateVersion(String token, String build, String master, long hostId, String packageUrl, long packageSize);

    /**
     * Requests that the slave synchronises its plugins with the master.
     *  
     * @param token               secure token for inter-host communication
     * @param master              url of the master requesting the sync
     * @param hostId              the slave host's id, for when it calls back
     * @param pluginRepositoryUrl base url of the master's plugin repository
     * @return true if synchronisation is required (and will be started
     *         asynchronously), false if the plugins are already in sync
     */
    boolean syncPlugins(String token, String master, long hostId, String pluginRepositoryUrl);

    HostStatus getStatus(String token, String master);

    /**
     * Synchronises the agent by processing all of the given messages.
     * Messages are converted to tasks, the tasks executed and the results
     * returned.
     *
     * @param master   url of the master
     * @param agentId  id of the agent these messages are for
     * @param messages messages to process
     * @return results corresponding results for each of the messages
     */
    List<SynchronisationMessageResult> synchronise(String token, String master, long agentId, List<SynchronisationMessage> messages);

    /**
     * A request to build a recipe on the slave, if the slave is currently idle.
     *
     * @param token   secure token for inter-agent communication
     * @param master  location of the master for return messages
     * @param agentHandle  handle of the agent, used in returned messages
     * @param request details of the recipe to build
     * @return true if the request was accepted, false of the slave was busy
     *
     * @throws InvalidTokenException if the given token does not match the
     * slave's
     */
    boolean build(String token, String master, long agentHandle, RecipeRequest request) throws InvalidTokenException;

    void cleanupRecipe(String token, AgentRecipeDetails recipeDetails) throws InvalidTokenException;

    void terminateRecipe(String token, long agentHandle, long recipeId) throws InvalidTokenException;

    ServerInfoModel getSystemInfo(String token, boolean includeDetailed) throws InvalidTokenException;

    List<CustomLogRecord> getRecentMessages(String token) throws InvalidTokenException;

    List<ResourceConfiguration> discoverResources(String token);

    void garbageCollect();

    /**
     * List the path relative to the base directory of the defined recipe.
     *
     * @param token     secure token for inter-agent communication
     * @param details   used to identify the base directory
     * @param path      path relative to the base directory
     *
     * @return a list of file info instances representing the requested listing.
     */
    List<FileInfo> getFileInfos(String token, AgentRecipeDetails details, String path);

    /**
     * Retrieve the file info for the path relative to the base directory of the
     * defined recipe.
     *
     * @param token     secure token for inter-agent communication
     * @param details   the recipe details
     * @param path      the path relative to the recipes base directory
     *
     * @return a file info object for the requested path.
     */
    FileInfo getFileInfo(String token, AgentRecipeDetails details, String path);

    /**
     * Executes a command on the slave.
     *
     * @param token secure token for inter-agent communication
     * @param master the master URL
     * @param context execution context for resolving properties in the workingDir
     * @param commandLine command (and arguments) to run
     * @param workingDir if not null, directory in which to run the command (may contain properties)
     * @param streamId id of the stream to use in {@link com.zutubi.pulse.core.events.GenericOutputEvent}s
     * @param timeout if non-zero, a time limit in seconds to apply to the command
     */
    void runCommand(String token, String master, PulseExecutionContext context, List<String> commandLine, String workingDir, long streamId, int timeout);
}
