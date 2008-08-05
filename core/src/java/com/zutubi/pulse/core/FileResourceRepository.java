package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Here be resources.  Yar.
 */
public class FileResourceRepository implements ResourceRepository
{
    private static final Logger LOG = Logger.getLogger(FileResourceRepository.class);

    private Map<String, Resource> resources = new TreeMap<String, Resource>();

    private File resourceDef;

    public FileResourceRepository()
    {
    }

    public void addResource(Resource r)
    {
        resources.put(r.getName(), r);
    }

    public boolean hasResource(ResourceRequirement requirement)
    {
        String name = requirement.getResource();
        String version = requirement.getVersion();

        Resource r = getResource(name);
        if (r == null)
        {
            return false;
        }

        return requirement.isDefaultVersion() || r.getVersion(version) != null;
    }

    public boolean hasResource(String name)
    {
        return resources.containsKey(name);
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

    public void refresh() throws IllegalAccessException, IOException, PulseException, InvocationTargetException
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
