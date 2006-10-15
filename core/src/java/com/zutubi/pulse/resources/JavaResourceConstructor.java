package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.util.SystemUtils;

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
        return isResourceHome(new File(home));
    }

    public boolean isResourceHome(File home)
    {
        if (!home.isDirectory())
        {
            return false;
        }

        // we expect the ant binary to be in the bin directory.
        File bin = new File(home, "bin");
        if (SystemUtils.isWindows())
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

            version.addProperty(new ResourceProperty(JAVA_HOME, home.getCanonicalPath(), true, false));

            File binDir = new File(home, "bin");
            version.addProperty(new ResourceProperty(JAVA_BIN_DIR, binDir.getCanonicalPath(), false, true));

            if (SystemUtils.isWindows())
            {
                File bin = new File(binDir, "java.exe");
                version.addProperty(new ResourceProperty(JAVA_BIN, bin.getCanonicalPath(), false, false));
            }
            else
            {
                File bin = new File(binDir, "java");
                version.addProperty(new ResourceProperty(JAVA_BIN, bin.getCanonicalPath(), false, false));
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

}
