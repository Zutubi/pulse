package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.GenericReference;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.ReferenceResolver;
import com.zutubi.pulse.core.ResolutionException;
import com.zutubi.pulse.core.engine.api.HashReferenceMap;
import com.zutubi.pulse.core.engine.api.ReferenceMap;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 * The server recipe paths:
 * <p/>
 * system/recipes/xyz/work
 * /output
 * <p/>
 * where xyz is the recipe identifier.
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

    public ServerRecipePaths(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern, File dataDir)
    {
        this(new AgentRecipeDetails(projectHandle, project, recipeId, incremental, persistentPattern), dataDir);
    }

    public File getRecipesRoot()
    {
        return new File(dataDir, "recipes");
    }

    public File getRecipeRoot()
    {
        return new File(getRecipesRoot(), Long.toString(recipeDetails.getRecipeId()));
    }

    public File getPersistentWorkDir()
    {
        ReferenceMap references = new HashReferenceMap();
        references.add(new GenericReference<String>("data.dir", dataDir.getAbsolutePath()));
        references.add(new GenericReference<String>("project", WebUtils.formUrlEncode(recipeDetails.getProject())));
        references.add(new GenericReference<String>("project.handle", Long.toString(recipeDetails.getProjectHandle())));

        try
        {
            String path = ReferenceResolver.resolveReferences(recipeDetails.getPersistentPattern(), references, ReferenceResolver.ResolutionStrategy.RESOLVE_STRICT);
            return new File(path);
        }
        catch (ResolutionException e)
        {
            LOG.warning("Invalid persistent work directory '" + recipeDetails.getPersistentPattern() + "': " + e.getMessage(), e);
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
