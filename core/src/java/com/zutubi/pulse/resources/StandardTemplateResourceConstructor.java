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
public class StandardTemplateResourceConstructor implements ResourceConstructor
{
    private String resourceName;

    private String scriptName;

    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public void setScriptName(String scriptName)
    {
        this.scriptName = scriptName;
    }

    public String lookupHome()
    {
        return null;
    }

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
            return new File(bin, scriptName + ".bat").isFile();
        }
        else
        {
            return new File(bin, scriptName).isFile();
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
            Resource resource = new Resource(resourceName);

            // what is the version of this ant installation? Use the name of the home directory for this.
            ResourceVersion version = new ResourceVersion(home.getName());
            resource.add(version);
            resource.setDefaultVersion(version.getValue());

            version.addProperty(new ResourceProperty((resourceName + "_HOME").toUpperCase(), FileSystemUtils.normaliseSeparators(home.getCanonicalPath()), true, false, false));

            // configure the binary directory for this version.
            File binDir = new File(home, "bin");
            version.addProperty(new ResourceProperty(resourceName + ".bin.dir", FileSystemUtils.normaliseSeparators(binDir.getAbsolutePath()), false, true, false));

            if (SystemUtils.IS_WINDOWS)
            {
                version.addProperty(new ResourceProperty(resourceName + ".bin", FileSystemUtils.normaliseSeparators(new File(binDir, scriptName + ".bat").getCanonicalPath()), false, false, false));
            }
            else
            {
                version.addProperty(new ResourceProperty(resourceName + ".bin", FileSystemUtils.normaliseSeparators(new File(binDir, scriptName).getCanonicalPath()), false, false, false));
            }

            File lib = new File(home, "lib");
            if (lib.isDirectory())
            {
                version.addProperty(new ResourceProperty(resourceName + ".lib.dir", FileSystemUtils.normaliseSeparators(lib.getAbsolutePath()), false, false, false));
            }
            return resource;
        }
        catch (FileLoadException e)
        {
            // This should never happen. We are creating a new resource version, not updating a pre existing version.
            e.printStackTrace();
            return null;
        }
    }
}
