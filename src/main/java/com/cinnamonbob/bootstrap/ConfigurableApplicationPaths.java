package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class ConfigurableApplicationPaths implements ApplicationPaths
{
    private File contentRoot;
    private File configRoot;
    private File templateRoot;
    private File userConfigRoot;
    /**
     * @deprecated 
     */
    private File applicationRoot;
    
    public File getContentRoot()
    {
        return contentRoot;
    }

    public File getConfigRoot()
    {
        return configRoot;
    }

    public File getTemplateRoot()
    {
        return templateRoot;
    }

    public File getUserConfigRoot()
    {
        return userConfigRoot;
    }

    /**
     * @deprecated 
     */
    public File getApplicationRoot()
    {
        return applicationRoot;
    }

    public void setContentRoot(File contentRoot)
    {
        this.contentRoot = contentRoot;
    }

    public void setConfigRoot(File configRoot)
    {
        this.configRoot = configRoot;
    }

    public void setTemplateRoot(File templateRoot)
    {
        this.templateRoot = templateRoot;
    }

    public void setUserConfigRoot(File userConfigRoot)
    {
        this.userConfigRoot = userConfigRoot;
    }

    public void setApplicationRoot(File applicationRoot)
    {
        this.applicationRoot = applicationRoot;
    }
}
