package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import static com.zutubi.pulse.core.resources.StandardHomeDirectoryConstants.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 */
public class StandardHomeDirectoryResourceBuilder implements ResourceBuilder
{
    private static final Logger LOG = Logger.getLogger(StandardHomeDirectoryResourceBuilder.class);

    private String resourceName;
    private String binaryName;
    private boolean script;

    public StandardHomeDirectoryResourceBuilder(String resourceName)
    {
        this(resourceName, resourceName, true);
    }

    public StandardHomeDirectoryResourceBuilder(String resourceName, String binaryName, boolean script)
    {
        this.resourceName = resourceName;
        this.binaryName = binaryName;
        this.script = script;
    }

    public Resource buildResource(File home)
    {
        try
        {
            Resource resource = new Resource(resourceName);

            // What is the version of this installation? Use the name of the home directory for this.
            // TODO I think we can seriously improve on this, it is not very
            // TODO accurate in many common setups.
            ResourceVersion version = new ResourceVersion(home.getName());
            resource.add(version);
            resource.setDefaultVersion(version.getValue());

            version.addProperty(new ResourceProperty(convertResourceNameToEnvironmentVariable(resourceName), getNormalisedPath(home), true, false, false));

            // configure the binary directory for this version.
            String binDirPath = getNormalisedPath(getBinaryDirectory(home));
            version.addProperty(new ResourceProperty(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY_DIRECTORY, binDirPath, false, true, false));

            String binaryPath = getNormalisedPath(getBinaryFile(home, binaryName, script));
            version.addProperty(new ResourceProperty(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY, binaryPath, false, false, false));

            File lib = getLibraryDirectory(home);
            if (lib.isDirectory())
            {
                String libDirPath = FileSystemUtils.normaliseSeparators(lib.getAbsolutePath());
                version.addProperty(new ResourceProperty(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_LIBRARY_DIRECTORY, libDirPath, false, false, false));
            }

            return resource;
        }
        catch (FileLoadException e)
        {
            // This should never happen. We are creating a new resource version, not updating a pre existing version.
            LOG.severe(e);
            return null;
        }
        catch (IOException e)
        {
            // On any real error, we just don't create the resource: no real
            // harm is done.
            LOG.warning("I/O error discovering resource '" + resourceName + "': " + e.getMessage(), e);
            return null;
        }
    }

    private String getNormalisedPath(File file) throws IOException
    {
        return FileSystemUtils.normaliseSeparators(file.getCanonicalPath());
    }
}
