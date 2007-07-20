package com.zutubi.pulse;

import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.util.FileSystemUtils;

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
    private long id;
    private File dataDir;
    private String project;
    private String spec;
    private boolean incremental;

    public ServerRecipePaths(String project, String spec, long id, File dataDir, boolean incremental)
    {
        this.project = project;
        this.spec = spec;
        this.id = id;
        this.dataDir = dataDir;
        this.incremental = incremental;
    }

    private File getRecipesRoot()
    {
        return new File(dataDir, "recipes");
    }

    public File getRecipeRoot()
    {
        return new File(getRecipesRoot(), Long.toString(id));
    }

    public File getPersistentWorkDir()
    {
        return new File(dataDir, FileSystemUtils.composeFilename("work", encode(project), encode(spec)));
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
