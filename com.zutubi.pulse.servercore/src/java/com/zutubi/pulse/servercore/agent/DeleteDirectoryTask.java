package com.zutubi.pulse.servercore.agent;

import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.cleanup.FileDeletionService;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.tove.variables.HashVariableMap;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Map;

/**
 * A synchronisation task that deletes a directory.  If the directory does not
 * exist, the request is ignored (this should also handle retries).
 * <p/>
 * As deleting can take time, the directory is submitted to the file deletion
 * service to be cleaned asynchronously.
 */
public class DeleteDirectoryTask implements SynchronisationTask
{
    private static final Logger LOG = Logger.getLogger(DeleteDirectoryTask.class);
    
    private String agentDataDirectoryPattern;
    private String pathPattern;
    private Map<String, String> variables;
    private transient ConfigurationManager configurationManager;
    private transient FileDeletionService fileDeletionService;

    public DeleteDirectoryTask()
    {
    }

    /**
     * Creates a task to delete a directory.
     *
     * @param agentDataDirectoryPattern pattern specifying the location of the
     *                                  agent data directory
     * @param pathPattern pattern specifying the directory to delete; may
     *                    include references to variables passed to this
     *                    constructor and/or $(data.dir), which is provided on
     *                    the agent before resolving the variables
     * @param variables   variables used to resolve the path patterns (excludes
     *                    the data directory, which is known on the agent side)
     */
    public DeleteDirectoryTask(String agentDataDirectoryPattern, String pathPattern, Map<String, String> variables)
    {
        this.agentDataDirectoryPattern = agentDataDirectoryPattern;
        this.pathPattern = pathPattern;
        this.variables = variables;
    }

    public void execute()
    {
        String path = resolvePattern();
        if (path != null)
        {
            File dir = new File(path);
            if (dir.exists())
            {
                fileDeletionService.delete(dir, true);
            }
        }
    }

    private String resolvePattern()
    {
        VariableMap map = new HashVariableMap();
        for (Map.Entry<String, String> entry: variables.entrySet())
        {
            map.add(new GenericVariable<String>(entry.getKey(), entry.getValue()));
        }
        
        map.add(new GenericVariable<String>(ServerRecipePaths.PROPERTY_DATA_DIR, configurationManager.getUserPaths().getData().getAbsolutePath()));
        try
        {
            map.add(new GenericVariable<String>(ServerRecipePaths.PROPERTY_AGENT_DATA_DIR, VariableResolver.resolveVariables(agentDataDirectoryPattern, map, VariableResolver.ResolutionStrategy.RESOLVE_NON_STRICT)));
            return VariableResolver.resolveVariables(pathPattern, map);
        }
        catch (ResolutionException e)
        {
            // This should not happen with non-strict resolution.
            LOG.debug(e);
        }
        
        return null;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setFileDeletionService(FileDeletionService fileDeletionService)
    {
        this.fileDeletionService = fileDeletionService;
    }
}
