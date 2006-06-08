package com.zutubi.pulse.slave;

import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.slave.command.CleanupRecipeCommand;
import com.zutubi.pulse.slave.command.RecipeCommand;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;

/**
 */
public class SlaveServiceImpl implements SlaveService
{
    private static final Logger LOG = Logger.getLogger(SlaveServiceImpl.class);

    private SlaveThreadPool threadPool;
    private SlaveConfigurationManager configurationManager;
    private SlaveStartupManager startupManager;
    private SlaveRecipeProcessor slaveRecipeProcessor;
    private ServerMessagesHandler serverMessagesHandler;

    public int ping()
    {
        return Version.getVersion().getIntBuildNumber();
    }

    public boolean build(String master, long slaveId, RecipeRequest request)
    {
        // TODO: dev-distributed: check queue, return true iff queue is empty
        RecipeCommand command = new RecipeCommand(master, slaveId, request);
        ComponentContext.autowire(command);
        ErrorHandlingRunnable runnable = new ErrorHandlingRunnable(master, request.getId(), command);
        ComponentContext.autowire(runnable);

        threadPool.executeCommand(runnable);
        return true;
    }

    public void cleanupRecipe(long recipeId)
    {
        CleanupRecipeCommand command = new CleanupRecipeCommand(recipeId);
        // TODO more dodgy wiring :-/
        ComponentContext.autowire(command);
        threadPool.executeCommand(command);
    }

    public void terminateRecipe(long recipeId)
    {
        // Do this request synchronously
        slaveRecipeProcessor.terminateRecipe(recipeId);
    }

    public SystemInfo getSystemInfo()
    {
        return SystemInfo.getSystemInfo(configurationManager, startupManager);
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return serverMessagesHandler.takeSnapshot();
    }

    public void setThreadPool(SlaveThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }

    public void setSlaveRecipeProcessor(SlaveRecipeProcessor slaveRecipeProcessor)
    {
        this.slaveRecipeProcessor = slaveRecipeProcessor;
    }

    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setStartupManager(SlaveStartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }
}
