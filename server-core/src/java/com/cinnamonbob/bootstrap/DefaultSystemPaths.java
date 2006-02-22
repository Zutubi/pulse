package com.cinnamonbob.bootstrap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class DefaultSystemPaths implements SystemPaths
{
    private final File bobInstall;

    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private List<File> templateRoots;

    public DefaultSystemPaths(File bobHome)
    {
        this.bobInstall = bobHome;
    }

    public File getSystemRoot()
    {
        if (systemRoot == null)
        {
            systemRoot = new File(bobInstall, "system");
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

}
