package com.zutubi.pulse;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;
import java.util.List;
import java.util.LinkedList;

/**
 */
public class ResourceDiscoverer
{
    public List<Resource> discover()
    {
        List<Resource> result = new LinkedList<Resource>();
        discoverAnt(result);
        discoverMake(result);
        discoverMaven2(result);
        discoverJava(result);
        return result;
    }

    private void discoverAnt(List<Resource> resources)
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

            resources.add(antResource);
        }
    }

    private void discoverMake(List<Resource> resources)
    {
        File makeBin = SystemUtils.findInPath("make");
        if (makeBin != null)
        {
            Resource makeResource = new Resource("make");
            makeResource.addProperty(new Property("make.bin", makeBin.getAbsolutePath()));
            resources.add(makeResource);
        }
    }

    private void discoverMaven2(List<Resource> resources)
    {
        File mvn = null;
        if (SystemUtils.isWindows())
        {
            mvn = SystemUtils.findInPath("mvn.bat");
        }
        else
        {
            mvn = SystemUtils.findInPath("mvn");
        }
                    
        if (mvn != null)
        {
            Resource mvnResource = new Resource("maven2");
            mvnResource.addProperty(new Property("maven2.bin", mvn.getAbsolutePath()));
            resources.add(mvnResource);
        }
    }

    private void discoverJava(List<Resource> resources)
    {
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
            resources.add(javaResource);
        }
    }
}
