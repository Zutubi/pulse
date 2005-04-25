package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class DefaultApplicationPaths implements ApplicationPaths
{

    private final File bobHome;

    private File contentRoot;
    private File configRoot;
    private File templateRoot;

    public DefaultApplicationPaths(File bobHome)
    {
        this.bobHome = bobHome;
    }

    public File getApplicationRoot()
    {
        return bobHome;
    }

    public File getContentRoot()
    {
        if (contentRoot == null)
        {
            contentRoot = new File(bobHome, "system" + File.separatorChar + "www");
        }
        return contentRoot;
    }

    public File getConfigRoot()
    {
        if (configRoot == null)
        {
            configRoot = new File(bobHome, "system" + File.separatorChar + "config");
        }
        return configRoot;
    }

    public File getTemplateRoot()
    {
        if (templateRoot == null)
        {
            templateRoot = new File(bobHome, "system" + File.separatorChar + "templates");
        }
        return templateRoot;
    }
}
