package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.resources.api.CompositeResourceLocator;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.SimpleBinaryResourceLocator;
import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceLocator;

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
    /**
     * Run the resource discovery process, returning a list of all the discovered resources.
     *
     * @return list of discovered resources.
     */
    public List<ResourceConfiguration> discover()
    {
        CompositeResourceLocator locator = new CompositeResourceLocator(
                new StandardHomeDirectoryResourceLocator("ant", true),
                new StandardHomeDirectoryResourceLocator("java", false),
                new StandardHomeDirectoryResourceLocator("maven", true),
                new StandardHomeDirectoryResourceLocator("maven2", "MAVEN2_HOME", "mvn", true),
                new SimpleBinaryResourceLocator("make"),
                new MsBuildResourceLocator()
        );
        return locator.locate();
    }

    public void discoverAndAdd(InMemoryResourceRepository repository)
    {
        List<ResourceConfiguration> resources = discover();
        for(ResourceConfiguration r: resources)
        {
            if(!repository.hasResource(r.getName()))
            {
                repository.addResource(r);
            }
        }
    }
}
