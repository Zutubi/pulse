package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.util.FileSystemUtils;
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
    private static final String PROPERTY_PERSISTENT_WORK_DIR = "pulse.persistent.work.dir";
    private static final String DEFAULT_PERSISTENT_WORK_DIR = "${data}/work/${project}";

    private static final Logger LOG = Logger.getLogger(ServerRecipePaths.class);

    private long id;
    private File dataDir;
    private String project;
    private boolean incremental;

    public ServerRecipePaths(String project, long id, File dataDir, boolean incremental)
    {
        this.project = project;
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
        String pattern = System.getProperty(PROPERTY_PERSISTENT_WORK_DIR, DEFAULT_PERSISTENT_WORK_DIR);
        Scope scope = new PulseScope();
        scope.add(new Property("data", dataDir.getAbsolutePath()));
        scope.add(new Property("project", encode(project)));

        try
        {
            String path = VariableHelper.replaceVariables(pattern, scope, true);
            return new File(path);
        }
        catch (FileLoadException e)
        {
            LOG.warning("Invalid persistent work directory '" + pattern + "': " + e.getMessage(), e);
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
