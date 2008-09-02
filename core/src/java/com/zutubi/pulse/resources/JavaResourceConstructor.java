package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.util.SystemUtils;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public class JavaResourceConstructor implements ResourceConstructor
{
    private static final String JAVA_HOME = "JAVA_HOME";
    private static final String JAVA_BIN_DIR = "java.bin.dir";
    private static final String JAVA_BIN = "java.bin";
    private static final String RESOURCE_NAME = "java";


    public boolean isResourceHome(String home)
    {
        return home != null && isResourceHome(new File(home));
    }

    public boolean isResourceHome(File home)
    {
        if (home == null || !home.isDirectory())
        {
            return false;
        }

        // we expect the ant binary to be in the bin directory.
        File bin = new File(home, "bin");
        if (SystemUtils.IS_WINDOWS)
        {
            return new File(bin, "java.exe").isFile();
        }
        else
        {
            return new File(bin, "java").isFile();
        }
    }


    public Resource createResource(String home) throws IOException
    {
        return createResource(new File(home));
    }

    public Resource createResource(File home) throws IOException
    {
        try
        {
            Resource javaResource = new Resource(RESOURCE_NAME);

            ResourceVersion version = new ResourceVersion(home.getName());
            javaResource.add(version);
            javaResource.setDefaultVersion(version.getValue());

            version.addProperty(new ResourceProperty(JAVA_HOME, FileSystemUtils.normaliseSeparators(home.getCanonicalPath()), true, false, false));

            File binDir = new File(home, "bin");
            version.addProperty(new ResourceProperty(JAVA_BIN_DIR, FileSystemUtils.normaliseSeparators(binDir.getCanonicalPath()), false, true, false));

            if (SystemUtils.IS_WINDOWS)
            {
                File bin = new File(binDir, "java.exe");
                version.addProperty(new ResourceProperty(JAVA_BIN, FileSystemUtils.normaliseSeparators(bin.getCanonicalPath()), false, false, false));
            }
            else
            {
                File bin = new File(binDir, "java");
                version.addProperty(new ResourceProperty(JAVA_BIN, FileSystemUtils.normaliseSeparators(bin.getCanonicalPath()), false, false, false));
            }
            return javaResource;
        }
        catch (FileLoadException e)
        {
            // This should never happen. We are creating a new resource version, not updating a pre existing version.
            e.printStackTrace();
            return null;
        }
    }


    public String lookupHome()
    {
        // TODO: look for java on the path.
        // look for JAVA_HOME in the environment.
        return System.getenv(JAVA_HOME);
    }
}
