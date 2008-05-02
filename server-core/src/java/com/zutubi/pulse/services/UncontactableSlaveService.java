package com.zutubi.pulse.services;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.filesystem.FileInfo;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.resources.ResourceConstructor;

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

    public SlaveStatus getStatus(String token, String master)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public boolean build(String token, String master, long slaveId, RecipeRequest request, BuildContext context) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public void cleanupRecipe(String token, String project, String spec, long recipeId, boolean incremental) throws InvalidTokenException
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public void terminateRecipe(String token, long recipeId) throws InvalidTokenException
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

    public List<Resource> discoverResources(String token)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public FileInfo getFileInfo(String token, String path)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public String[] listRoots(String token)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public Resource createResource(ResourceConstructor constructor, String path)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }

    public boolean isResourceHome(ResourceConstructor constructor, String path)
    {
        throw new RuntimeException("Agent configuration is invalid: " + errorMessage);
    }
}
