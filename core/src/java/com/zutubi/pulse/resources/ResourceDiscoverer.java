package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.util.SystemUtils;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The resource discoverer is responsible for the automatic discovery of common resources.
 *
 * The configuration of the resources along with the smarts about the various resources is delegated to
 * the ResourceConstructor implementations.
 * 
 */
//TODO: extend this to allow the registeration of resource constructors.
public class ResourceDiscoverer
{
    private static final Logger LOG = Logger.getLogger(ResourceDiscoverer.class);

    /**
     * Run the resource discovery process, returning a list of all the discovered resources.
     *
     * @return list of discovered resources.
     */
    public List<Resource> discover()
    {
        List<Resource> result = new LinkedList<Resource>();

        for (ResourceConstructor constructor : getConstructors())
        {
            String home = constructor.lookupHome();
            if (constructor.isResourceHome(home))
            {
                try
                {
                    result.add(constructor.createResource(home));
                }
                catch (IOException e)
                {
                    LOG.warning(e);
                }
            }
        }

        discoverMake(result);

        return result;
    }

    private List<ResourceConstructor> getConstructors()
    {
        List<ResourceConstructor> constructors = new LinkedList<ResourceConstructor>();
        constructors.add(new AntResourceConstructor());
        constructors.add(new JavaResourceConstructor());
        constructors.add(new MavenResourceConstructor());
        constructors.add(new Maven2ResourceConstructor());
        return constructors;
    }

    private void discoverMake(List<Resource> resources)
    {
        //TODO: merge this resource in with the existing resoruce constructors.  May require some reworking of the interfaces
        //TODO: since current resource constructors require home directories.
        File makeBin = SystemUtils.findInPath("make");
        if (makeBin != null)
        {
            Resource makeResource = new Resource("make");
            makeResource.addProperty(new ResourceProperty("make.bin", FileSystemUtils.normaliseSeparators(makeBin.getAbsolutePath()), false, false, false));
            resources.add(makeResource);
        }
    }
}
