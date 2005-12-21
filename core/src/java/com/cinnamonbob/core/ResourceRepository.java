package com.cinnamonbob.core;

import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Here be resources.  Yar.
 */
public class ResourceRepository
{
    private static final Logger LOG = Logger.getLogger(ResourceRepository.class);

    private Map<String, Resource> resources = new TreeMap<String, Resource>();

    private File resourceDef;

    public ResourceRepository()
    {
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

    public void initialise()
    {
        try
        {
            refresh();
        }
        catch (Exception e)
        {
            LOG.warning(e.getMessage(), e);
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
                ResourceFileLoader.load(input, this);
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
