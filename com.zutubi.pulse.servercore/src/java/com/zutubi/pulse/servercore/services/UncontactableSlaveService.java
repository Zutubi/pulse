package com.zutubi.pulse.servercore.services;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;

import java.util.List;

/**
 * A slave service that is used when it is not possible to create a hessian
 * proxy.
 */
public class UncontactableSlaveService implements SlaveService
{
    private String errorMessage;

    public UncontactableSlaveService(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public int ping()
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public boolean updateVersion(String token, String build, String master, long id, String packageUrl, long packageSize)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public HostStatus getStatus(String token, String master)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public List<SynchronisationMessageResult> synchronise(String token, String master, long agentId, List<SynchronisationMessage> messages)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public boolean build(String token, String master, long handle, RecipeRequest request) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public void cleanupRecipe(String token, AgentRecipeDetails recipeDetails) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public void terminateRecipe(String token, long agentHandle, long recipeId) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public SystemInfo getSystemInfo(String token) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public List<CustomLogRecord> getRecentMessages(String token) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public List<ResourceConfiguration> discoverResources(String token)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public void garbageCollect()
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public List<FileInfo> getFileInfos(String token, AgentRecipeDetails recipeDetails, String path)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public FileInfo getFileInfo(String token, AgentRecipeDetails recipeDetails, String path)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }
}
