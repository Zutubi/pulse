/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class DefaultSystemPaths implements SystemPaths
{
    private final File pulseInstall;

    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private File logRoot;
    private List<File> templateRoots;
    private File tmpRoot;

    public DefaultSystemPaths(File pulseHome)
    {
        this.pulseInstall = pulseHome;
    }

    public File getSystemRoot()
    {
        if (systemRoot == null)
        {
            systemRoot = new File(pulseInstall, "system");
        }
        return systemRoot;
    }

    public File getContentRoot()
    {
        if (contentRoot == null)
        {
            contentRoot = new File(getSystemRoot(), "www");
        }
        return contentRoot;
    }

    public File getConfigRoot()
    {
        if (configRoot == null)
        {
            configRoot = new File(getSystemRoot(), "config");
        }
        return configRoot;
    }

    public File getLogRoot()
    {
        if (logRoot == null)
        {
            logRoot = new File(getSystemRoot(), "log");
        }
        return logRoot;
    }

    public List<File> getTemplateRoots()
    {
        if (templateRoots == null)
        {
            templateRoots = new LinkedList<File>();
            templateRoots.add(new File(getSystemRoot(), "templates"));
            templateRoots.add(new File(getSystemRoot(), "www"));
        }
        return templateRoots;
    }

    public File getTmpRoot()
    {
        if (tmpRoot == null)
        {
            tmpRoot = new File(getSystemRoot(), "tmp");
        }
        return tmpRoot;
    }
}
