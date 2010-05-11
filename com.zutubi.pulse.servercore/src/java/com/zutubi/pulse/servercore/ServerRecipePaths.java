package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;

import static com.zutubi.tove.variables.VariableResolver.ResolutionStrategy.RESOLVE_STRICT;

/**
 * The server recipe paths, which by default live at:
 * <ul>
 *   <li>$(data.dir)/agents/$(agent.handle)/recipes/$(recipe.id) - for transient recipes, and</li>
 *   <li>$(data.dir)/agents/$(agent.handle)/work/$(project.handle)/$(stage.handle) - for persistent recipes.</li>
 * </ul>
 */
public class ServerRecipePaths implements RecipePaths
{
    private static final Logger LOG = Logger.getLogger(ServerRecipePaths.class);

    public static final String PROPERTY_DATA_DIR = "data.dir";
    public static final String PROPERTY_AGENT_DATA_DIR = "agent.data.dir";
    
    private AgentRecipeDetails recipeDetails;
    private File dataDir;

    public ServerRecipePaths(AgentRecipeDetails recipeDetails, File dataDir)
    {
        this.recipeDetails = recipeDetails;
        this.dataDir = dataDir;
    }

    private File getAgentDataDir()
    {
        VariableMap references = recipeDetails.createPathVariableMap();
        references.add(new GenericVariable<String>(PROPERTY_DATA_DIR, dataDir.getAbsolutePath()));

        try
        {
            String path = VariableResolver.resolveVariables(recipeDetails.getAgentDataPattern(), references, RESOLVE_STRICT);
            return new File(path);
        }
        catch (ResolutionException e)
        {
            LOG.warning("Invalid agent data directory '" + recipeDetails.getAgentDataPattern() + "': " + e.getMessage(), e);
            return new File(dataDir, FileSystemUtils.composeFilename("agents", Long.toString(recipeDetails.getAgentHandle())));
        }
    }

    public File getRecipesRoot()
    {
        return new File(getAgentDataDir(), "recipes");
    }

    public File getRecipeRoot()
    {
        return new File(getRecipesRoot(), Long.toString(recipeDetails.getRecipeId()));
    }

    public File getPersistentWorkDir()
    {
        VariableMap references = recipeDetails.createPathVariableMap();
        references.add(new GenericVariable<String>(PROPERTY_AGENT_DATA_DIR, getAgentDataDir().getAbsolutePath()));
        references.add(new GenericVariable<String>(PROPERTY_DATA_DIR, dataDir.getAbsolutePath()));

        try
        {
            String path = VariableResolver.resolveVariables(recipeDetails.getProjectPersistentPattern(), references, RESOLVE_STRICT);
            return new File(path);
        }
        catch (ResolutionException e)
        {
            LOG.warning("Invalid persistent work directory '" + recipeDetails.getProjectPersistentPattern() + "': " + e.getMessage(), e);
            return new File(getAgentDataDir(), FileSystemUtils.composeFilename("work", Long.toString(recipeDetails.getProjectHandle()), Long.toString(recipeDetails.getStageHandle())));
        }
    }

    public File getBaseDir()
    {
        if (recipeDetails.isIncremental())
        {
            return getPersistentWorkDir();
        }
        else
        {
            return new File(getRecipeRoot(), "base");
        }
    }

    public File getOutputDir()
    {
        return new File(getRecipeRoot(), "output");
    }
}
