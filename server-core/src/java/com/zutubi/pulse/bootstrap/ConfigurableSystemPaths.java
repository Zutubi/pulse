package com.zutubi.pulse.bootstrap;

import java.io.File;
import java.util.List;

/**
 * A convenience object that allows for the System and User paths to be specified
 */
public class ConfigurableSystemPaths implements SystemPaths
{
    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private File logRoot;
    private File tmpRoot;
    private List<File> templateRoots;

    public File getSystemRoot()
    {
        return systemRoot;
    }

    public File getContentRoot()
    {
        return contentRoot;
    }

    public File getConfigRoot()
    {
        return configRoot;
    }

    public File getLogRoot()
    {
        return logRoot;
    }

    public List<File> getTemplateRoots()
    {
        return templateRoots;
    }

    public File getTmpRoot()
    {
        return tmpRoot;
    }

    public void setTemplateRoots(List<File> templateRoots)
    {
        this.templateRoots = templateRoots;
    }

    public void setSystemRoot(File systemRoot)
    {
        this.systemRoot = systemRoot;
    }

    public void setContentRoot(File contentRoot)
    {
        this.contentRoot = contentRoot;
    }

    public void setConfigRoot(File configRoot)
    {
        this.configRoot = configRoot;
    }

    public void setLogRoot(File logRoot)
    {
        this.logRoot = logRoot;
    }

    public void setTmpRoot(File tmpRoot)
    {
        this.tmpRoot = tmpRoot;
    }
}
