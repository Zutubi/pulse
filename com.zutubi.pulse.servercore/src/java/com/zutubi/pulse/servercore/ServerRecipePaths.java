package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.GenericReference;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.ReferenceResolver;
import static com.zutubi.pulse.core.ReferenceResolver.ResolutionStrategy.RESOLVE_STRICT;
import com.zutubi.pulse.core.ResolutionException;
import com.zutubi.pulse.core.engine.api.HashReferenceMap;
import com.zutubi.pulse.core.engine.api.ReferenceMap;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 * The server recipe paths, which by default live at:
 * <ul>
 *   <li>${data.dir}/agents/${agent}/recipes/${recipe.id} - for transient recipes, and</li>
 *   <li>${data.dir}/agents/${agent}/work/${project} - for persistent recipes.</li>
 * </ul>
 */
public class ServerRecipePaths implements RecipePaths
{
    private static final Logger LOG = Logger.getLogger(ServerRecipePaths.class);

    private AgentRecipeDetails recipeDetails;
    private File dataDir;

    public ServerRecipePaths(AgentRecipeDetails recipeDetails, File dataDir)
    {
        this.recipeDetails = recipeDetails;
        this.dataDir = dataDir;
    }

    public ServerRecipePaths(long agentHandle, String agentName, String agentDataPattern, long projectHandle, String project, long recipeId, boolean incremental, String projectPersistentPattern, File dataDir)
    {
        this(new AgentRecipeDetails(agentHandle, agentName, agentDataPattern, projectHandle, project, recipeId, incremental, projectPersistentPattern), dataDir);
    }

    private File getAgentDataDir()
    {
        ReferenceMap references = new HashReferenceMap();
        references.add(new GenericReference<String>("data.dir", dataDir.getAbsolutePath()));
        references.add(new GenericReference<String>("agent", WebUtils.formUrlEncode(recipeDetails.getAgent())));
        references.add(new GenericReference<String>("agent.handle", Long.toString(recipeDetails.getAgentHandle())));

        try
        {
            String path = ReferenceResolver.resolveReferences(recipeDetails.getAgentDataPattern(), references, RESOLVE_STRICT);
            return new File(path);
        }
        catch (ResolutionException e)
        {
            LOG.warning("Invalid agent data directory '" + recipeDetails.getAgentDataPattern() + "': " + e.getMessage(), e);
            return new File(dataDir, FileSystemUtils.composeFilename("agents", WebUtils.formUrlEncode(recipeDetails.getAgent())));
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
        ReferenceMap references = new HashReferenceMap();
        references.add(new GenericReference<String>("agent.data.dir", getAgentDataDir().getAbsolutePath()));
        references.add(new GenericReference<String>("data.dir", dataDir.getAbsolutePath()));
        references.add(new GenericReference<String>("project", WebUtils.formUrlEncode(recipeDetails.getProject())));
        references.add(new GenericReference<String>("project.handle", Long.toString(recipeDetails.getProjectHandle())));

        try
        {
            String path = ReferenceResolver.resolveReferences(recipeDetails.getProjectPersistentPattern(), references, RESOLVE_STRICT);
            return new File(path);
        }
        catch (ResolutionException e)
        {
            LOG.warning("Invalid persistent work directory '" + recipeDetails.getProjectPersistentPattern() + "': " + e.getMessage(), e);
            return new File(dataDir, FileSystemUtils.composeFilename("work", WebUtils.formUrlEncode(recipeDetails.getProject())));
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
