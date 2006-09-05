package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.classloader.JarClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The archive plugin loader supports loading plugins from jar and zip files.
 *
 */
public class ArchivePluginLoader extends PluginLoaderSupport
{
    private static final Logger LOG = Logger.getLogger(ArchivePluginLoader.class.getName());

    private File archiveFile = null;

    public ArchivePluginLoader()
    {
    }

    public ArchivePluginLoader(File pluginFile)
    {
        setArchive(pluginFile);
    }

    /**
     * Specify the archive file (zip or jar) from which this plugin loader will load
     * the plugin.
     *
     * @param file
     */
    public void setArchive(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException();
        }
        if (!file.isFile())
        {
            throw new IllegalArgumentException();
        }

        this.archiveFile = file;
    }

    /**
     * Load the plugin from the configured archive file.
     *
     * @return
     *
     * @see #setArchive(java.io.File)
     */
    public List<Plugin> loadPlugins()
    {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null)
        {
            parent = getClass().getClassLoader();
        }

        JarClassLoader loader = null;
        try
        {
            loader = new JarClassLoader(archiveFile, parent);

            // use find resource so that we only look in the archive for the resource.
            URL descriptor = loader.findResource(resource);
            if (descriptor == null)
            {
                return new LinkedList<Plugin>();
            }
            Plugin plugin = loadPlugin(descriptor, loader, archiveFile.toURL());
            if (plugin == null)
            {
                return new LinkedList<Plugin>();
            }
            return Arrays.asList(plugin);
        }
        catch (IOException ioe)
        {
            try
            {
                LOG.log(Level.WARNING, ioe.getMessage(), ioe);
                return new LinkedList<Plugin>();
            }
            finally
            {
                // close the loader if there is an exception since the plugins will not be created to clean up
                // the resources..
                loader.close();
            }
        }
    }
}
