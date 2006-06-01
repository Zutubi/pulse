package com.zutubi.pulse;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;

/**
 */
public class ResourceDiscoverer implements Runnable
{
    private ResourceRepository resourceRepository;

    public ResourceDiscoverer()
    {
    }

    public ResourceDiscoverer(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }

    public void run()
    {
        discoverAnt();
        discoverMake();
        discoverMaven2();
        discoverJava();
    }

    private void discoverAnt()
    {
        if (!resourceRepository.hasResource("ant"))
        {
            String home = System.getenv("ANT_HOME");
            if (home != null)
            {
                Resource antResource = new Resource("ant");
                antResource.addProperty(new Property("ant.home", home));
                File antBin;

                if (SystemUtils.isWindows())
                {
                    antBin = new File(home, "bin/ant.bat");
                }
                else
                {
                    antBin = new File(home, "bin/ant");
                }

                if (antBin.isFile())
                {
                    antResource.addProperty(new Property("ant.bin", antBin.getAbsolutePath()));
                }

                File antLib = new File(home, "lib");
                if (antLib.isDirectory())
                {
                    antResource.addProperty(new Property("ant.lib.dir", antLib.getAbsolutePath()));
                }

                resourceRepository.addResource(antResource);
            }
        }
    }

    private void discoverMake()
    {
        if (!resourceRepository.hasResource("make"))
        {
            File makeBin = SystemUtils.findInPath("make");
            if (makeBin != null)
            {
                Resource makeResource = new Resource("make");
                makeResource.addProperty(new Property("make.bin", makeBin.getAbsolutePath()));
                resourceRepository.addResource(makeResource);
            }
        }
    }

    private void discoverMaven2()
    {
        if (!resourceRepository.hasResource("maven2"))
        {
            File mvn = SystemUtils.findInPath("mvn");
            if (mvn != null)
            {
                Resource mvnResource = new Resource("maven2");
                mvnResource.addProperty(new Property("maven2.bin", mvn.getAbsolutePath()));
                resourceRepository.addResource(mvnResource);
            }
        }
    }

    private void discoverJava()
    {
        if (resourceRepository.hasResource("java"))
        {
            return;
        }

        //TODO: look for java on the path.

        // look for JAVA_HOME in the environment.
        String home = System.getenv("JAVA_HOME");

        Resource javaResource = new Resource("java");
        javaResource.addProperty(new Property("java.home", home));

        File javaBin;
        if (SystemUtils.isWindows())
        {
            javaBin = new File(home, "bin/java.exe");
        }
        else
        {
            javaBin = new File(home, "bin/java");
        }

        if (javaBin.isFile())
        {
            javaResource.addProperty(new Property("java.bin", javaBin.getAbsolutePath()));
            resourceRepository.addResource(javaResource);
        }
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }
}
