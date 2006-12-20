package com.zutubi.plugins.repositories;

import com.zutubi.plugins.ComponentDescriptorFactory;
import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.PluginRepository;
import com.zutubi.plugins.internal.loaders.ArchivePluginLoader;
import com.zutubi.plugins.utils.FileOnlyFilter;
import com.zutubi.plugins.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class LocalPluginRepository implements PluginRepository
{
    private File repositoryDirectory = null;

    private Map<String, Plugin> loadedPlugins = new HashMap<String, Plugin>();

    private ComponentDescriptorFactory descriptorFactory;

    /**
     * Indicates whether or not we have scanned the local repository directory and loaded files from it.
     */
    private boolean scannedForPlugins = false;

    public LocalPluginRepository(File repositoryDirectory)
    {
        this.repositoryDirectory = repositoryDirectory;
    }

    public void setDescriptorFactory(ComponentDescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    // list the plugins in the repository. separate the listing from the loading to allow for a remote plugin repository.

    /**
     * Scan the configured repository directory, loading any plugins that are found.
     */
    public void scanForPlugins()
    {
        if (!scannedForPlugins)
        {
            for (File file : repositoryDirectory.listFiles(new FileOnlyFilter()))
            {
                loadPlugin(file);
            }
            scannedForPlugins = true;
        }
    }

    /**
     * Load the plugin from the specified file.
     *
     * @param f
     */
    protected void loadPlugin(File f)
    {
        ArchivePluginLoader loader = new ArchivePluginLoader();
        loader.setComponentDescriptorFactory(descriptorFactory);
        loader.setArchive(f);

        for (Plugin plugin : loader.loadPlugins())
        {
            if (containsPlugin(plugin))
            {
                throw new IllegalStateException("plugin already loaded");
            }
            loadedPlugins.put(plugin.getKey(), plugin);
        }
    }

    protected void unloadPlugin(Plugin plugin)
    {
        if (!loadedPlugins.containsKey(plugin.getKey()))
        {
            throw new IllegalArgumentException();
        }

        // remove plugin from map.
        loadedPlugins.remove(plugin.getKey());

        plugin.close();
    }

    public List<String> listPlugins()
    {
        return new LinkedList<String>(loadedPlugins.keySet());
    }

    public Plugin getPlugin(String key)
    {
        return loadedPlugins.get(key);
    }

    public List<Plugin> getPlugins()
    {
        return new LinkedList<Plugin>(loadedPlugins.values());
    }

    public boolean containsPlugin(Plugin plugin)
    {
        return loadedPlugins.containsKey(plugin.getKey());
    }

    public Plugin installPlugin(Plugin plugin)
    {
        try
        {
            // copy source into the plugins install directory.
            URL source = plugin.getSource();
            // get the file name...
            String fileName = source.getPath();
            if (fileName.contains("/"))
            {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }

            File repositoryPluginFile = new File(repositoryDirectory, fileName + "." + FileUtils.randomString(5));
            while (repositoryPluginFile.exists())
            {
                repositoryPluginFile = new File(repositoryDirectory, fileName + "." + FileUtils.randomString(5));
            }

            FileUtils.copyAndClose(source.openStream(), new FileOutputStream(repositoryPluginFile));

            // load the plugin from the install directory.
            loadPlugin(repositoryPluginFile);

            return loadedPlugins.get(plugin.getKey());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void uninstallPlugin(Plugin plugin)
    {
        if (plugin.isEnabled())
        {
            throw new IllegalArgumentException();
        }

        //TODO: this currently fails if multiple plugins are located in a single file.

        unloadPlugin(plugin);

        URL source = plugin.getSource();
        try
        {
            File installedPlugin = new File(source.toURI());
            if (!installedPlugin.delete())
            {
                throw new RuntimeException();
            }
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean supportsInstall()
    {
        return true;
    }

    public boolean supportsUninstall()
    {
        return true;
    }

    public void destory()
    {
        List<Plugin> plugins = new LinkedList<Plugin>(loadedPlugins.values());
        for (Plugin plugin : plugins)
        {
            unloadPlugin(plugin);
        }
    }
}
