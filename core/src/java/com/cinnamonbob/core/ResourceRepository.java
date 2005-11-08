package com.cinnamonbob.core;

import com.cinnamonbob.core.util.IOUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Here be resources.  Yar.
 */
public class ResourceRepository
{
    private static final Logger LOG = Logger.getLogger(ResourceRepository.class.getName());

    private Map<String, Resource> resources = new TreeMap<String, Resource>();

    private FileLoader fileLoader;

    private File resourceDef;

    public ResourceRepository()
    {

    }

    public ResourceRepository(FileLoader loader)
    {
        setFileLoader(loader);
    }

    public void load(InputStream input) throws BobException
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

    public void initialise()
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

    public void setUserConfigRoot(File userConfigRoot)
    {
        resourceDef = new File(userConfigRoot, "resources.xml");
    }
}
