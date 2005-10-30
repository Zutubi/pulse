package com.cinnamonbob.core;

import com.cinnamonbob.BobException;
import com.cinnamonbob.util.IOUtils;
import com.cinnamonbob.bootstrap.ConfigurationManager;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.InitializingBean;

/**
 * Here be resources.  Yar.
 */
public class ResourceRepository implements InitializingBean
{
    private static final Logger LOG = Logger.getLogger(ResourceRepository.class.getName());

    private Map<String, Resource> resources = new TreeMap<String, Resource>();

    private FileLoader fileLoader;

    private ConfigurationManager configurationManager;

    public void load(InputStream input) throws IllegalAccessException, IOException, InvocationTargetException, BobException
    {
        resources.clear();
        fileLoader.load(input, this);
    }

    public void addResource(Resource r)
    {
        resources.put(r.getName(), r);
    }

    public Resource getResource(String name)
    {
        return resources.get(name);
    }

    public List<String> getResourceNames()
    {
        return new LinkedList<String>(resources.keySet());
    }

    public void setFileLoader(FileLoader loader)
    {
        this.fileLoader = loader;

        loader.register("resource", Resource.class);
        loader.register("version", ResourceVersion.class);
        loader.register("property", Property.class);
    }

    public void afterPropertiesSet() throws Exception
    {
        try
        {
            refresh();
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void refresh() throws IllegalAccessException, IOException, BobException, InvocationTargetException
    {
        File configDir = configurationManager.getApplicationPaths().getUserConfigRoot();
        File resourceDef = new File(configDir, "resources.xml");
        if (resourceDef.exists())
        {
            InputStream input = null;
            try
            {
                input = new FileInputStream(resourceDef);
                load(input);
            }
            catch (FileNotFoundException e)
            {
                // noop.
            }
            finally
            {
                IOUtils.close(input);
            }
        }
    }

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configurationManager = configManager;
    }
}
