package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class TestApplicationPaths implements ApplicationPaths
{

    private File bobRoot = null;

    TestApplicationPaths(File bobRoot)
    {
        this.bobRoot = bobRoot;
    }

    public File getContentRoot()
    {
        return new File(bobRoot, "src" + File.separatorChar + "www");
    }

    public File getConfigRoot()
    {
        return new File(bobRoot, "src" + File.separatorChar + "main" + File.separatorChar + "config");
    }

    public File getTemplateRoot()
    {
        return new File(bobRoot, "src" + File.separatorChar + "templates");
    }

    public File getApplicationRoot()
    {
        return null;
    }
}
