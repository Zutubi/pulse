package com.zutubi.pulse;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;
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
            antResource.addProperty(new ResourceProperty("ANT_HOME", home, true, false));

            File binDir = new File(home, "bin");
            if (binDir.isDirectory())
            {
                antResource.addProperty(new ResourceProperty("ant.bin.dir", binDir.getAbsolutePath(), false, true));

                File bin;
                if(SystemUtils.isWindows())
                {
                    bin = new File("ant.bat");
                }
                else
                {
                    bin = new File(binDir, "ant");
                }

                if(bin.isFile())
                {
                    antResource.addProperty(new ResourceProperty("ant.bin", bin.getAbsolutePath(), false, false));
                }
            }

            File antLib = new File(home, "lib");
            if (antLib.isDirectory())
            {
                antResource.addProperty(new ResourceProperty("ant.lib.dir", antLib.getAbsolutePath(), false, false));
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
            makeResource.addProperty(new ResourceProperty("make.bin", makeBin.getAbsolutePath(), false, false));
            resources.add(makeResource);
        }
    }

    private void discoverMaven2(List<Resource> resources)
    {
        File mvn = SystemUtils.findInPath("mvn");

        if (mvn != null)
        {
            Resource mvnResource = new Resource("maven2");
            mvnResource.addProperty(new ResourceProperty("maven2.bin", mvn.getAbsolutePath(), false, false));
            resources.add(mvnResource);
        }
    }

    private void discoverJava(List<Resource> resources)
    {
        // TODO: look for java on the path.
        // look for JAVA_HOME in the environment.
        String home = System.getenv("JAVA_HOME");

        Resource javaResource = new Resource("java");
        javaResource.addProperty(new ResourceProperty("JAVA_HOME", home, true, false));

        File binDir = new File(home, "bin");
        if (binDir.isDirectory())
        {
            javaResource.addProperty(new ResourceProperty("java.bin.dir", binDir.getAbsolutePath(), false, true));

            File bin;
            if(SystemUtils.isWindows())
            {
                bin = new File("java.exe");
            }
            else
            {
                bin = new File(binDir, "java");
            }

            if(bin.isFile())
            {
                javaResource.addProperty(new ResourceProperty("java.bin", bin.getAbsolutePath(), false, false));
            }

        }

        resources.add(javaResource);
    }
}
