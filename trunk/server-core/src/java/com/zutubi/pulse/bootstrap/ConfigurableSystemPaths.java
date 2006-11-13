package com.zutubi.pulse.bootstrap;

import java.io.File;
import java.util.List;
import java.util.LinkedList;

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

    public void setTemplateRootStrings(List<String> roots)
    {
        templateRoots = new LinkedList<File>();
        for (String root : roots)
        {
            templateRoots.add(new File(root));
        }
    }

    public void setSystemRootString(String systemRoot)
    {
        this.systemRoot = new File(systemRoot);
    }

    public void setContentRootString(String contentRoot)
    {
        this.contentRoot = new File(contentRoot);
    }

    public void setConfigRootString(String configRoot)
    {
        this.configRoot = new File(configRoot);
    }

    public void setLogRootString(String logRoot)
    {
        this.logRoot = new File(logRoot);
    }

    public void setTmpRootString(String tmpRoot)
    {
        this.tmpRoot = new File(tmpRoot);
    }
}
