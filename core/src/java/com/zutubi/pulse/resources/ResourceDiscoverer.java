package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ResourceDiscoverer
{
    public List<Resource> discover()
    {
        List<Resource> result = new LinkedList<Resource>();
        discoverAnt(result);
        discoverMake(result);
        discoverMaven(result);
        discoverMaven2(result);
        discoverJava(result);
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

    private void discoverAnt(List<Resource> resources)
    {
        String home = System.getenv("ANT_HOME");
        AntResourceConstructor antConstructor = new AntResourceConstructor();
        if (antConstructor.isResourceHome(home))
        {
            try
            {
                resources.add(antConstructor.createResource(home));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void discoverMake(List<Resource> resources)
    {
        File makeBin = SystemUtils.findInPath("make");
        if (makeBin != null)
        {
            Resource makeResource = new Resource("make");
            makeResource.addProperty(new ResourceProperty("make.bin", makeBin.getAbsolutePath(), false, false, false));
            resources.add(makeResource);
        }
    }

    private void discoverMaven(List<Resource> resources)
    {
        String home = System.getenv("MAVEN_HOME");
        MavenResourceConstructor constructor = new MavenResourceConstructor();
        if (constructor.isResourceHome(home))
        {
            try
            {
                resources.add(constructor.createResource(home));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void discoverMaven2(List<Resource> resources)
    {
        String home = System.getenv("MAVEN2_HOME");
        Maven2ResourceConstructor constructor = new Maven2ResourceConstructor();
        if (constructor.isResourceHome(home))
        {
            try
            {
                resources.add(constructor.createResource(home));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void discoverJava(List<Resource> resources)
    {
        // TODO: look for java on the path.
        // look for JAVA_HOME in the environment.
        String home = System.getenv("JAVA_HOME");

        JavaResourceConstructor javaConstructor = new JavaResourceConstructor();
        if (javaConstructor.isResourceHome(home))
        {
            try
            {
                resources.add(javaConstructor.createResource(home));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
