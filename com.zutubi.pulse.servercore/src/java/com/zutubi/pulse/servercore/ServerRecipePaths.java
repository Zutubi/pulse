package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.ReferenceResolver;
import com.zutubi.pulse.core.ResolutionException;
import com.zutubi.pulse.core.engine.api.HashReferenceMap;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.engine.api.ReferenceMap;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    private long projectHandle;
    private long recipeId;
    private File dataDir;
    private String project;
    private boolean incremental;
    private String persistentPattern;

    public ServerRecipePaths(long projectHandle, String project, long recipeId, File dataDir, boolean incremental, String persistentPattern)
    {
        this.projectHandle = projectHandle;
        this.project = project;
        this.recipeId = recipeId;
        this.dataDir = dataDir;
        this.incremental = incremental;
        this.persistentPattern = persistentPattern;
    }

    public File getRecipesRoot()
    {
        return new File(dataDir, "recipes");
    }

    public File getRecipeRoot()
    {
        return new File(getRecipesRoot(), Long.toString(recipeId));
    }

    public File getPersistentWorkDir()
    {
        ReferenceMap references = new HashReferenceMap();
        references.add(new Property("data.dir", dataDir.getAbsolutePath()));
        references.add(new Property("project", encode(project)));
        references.add(new Property("project.handle", Long.toString(projectHandle)));

        try
        {
            String path = ReferenceResolver.resolveReferences(persistentPattern, references, ReferenceResolver.ResolutionStrategy.RESOLVE_STRICT);
            return new File(path);
        }
        catch (ResolutionException e)
        {
            LOG.warning("Invalid persistent work directory '" + persistentPattern + "': " + e.getMessage(), e);
            return new File(dataDir, FileSystemUtils.composeFilename("work", encode(project)));
        }
    }

    private String encode(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return s;
        }
    }

    public File getBaseDir()
    {
        if(incremental)
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
