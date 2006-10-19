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

    public boolean isResourceHome(String home)
    {
        return home != null && isResourceHome(new File(home));
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

            version.addProperty(new ResourceProperty((resourceName + "_HOME").toUpperCase(), home.getCanonicalPath(), true, false));

            // configure the binary directory for this version.
            File binDir = new File(home, "bin");
            version.addProperty(new ResourceProperty(resourceName + ".bin.dir", binDir.getAbsolutePath(), false, true));

            if (SystemUtils.isWindows())
            {
                version.addProperty(new ResourceProperty(resourceName + ".bin", new File(binDir, scriptName + ".bat").getCanonicalPath(), false, false));
            }
            else
            {
                version.addProperty(new ResourceProperty(resourceName + ".bin", new File(binDir, scriptName).getCanonicalPath(), false, false));
            }

            File lib = new File(home, "lib");
            if (lib.isDirectory())
            {
                version.addProperty(new ResourceProperty(resourceName + ".lib.dir", lib.getAbsolutePath(), false, false));
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
