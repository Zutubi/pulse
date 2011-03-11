package com.zutubi.pulse.core.resources.api;

import static com.zutubi.pulse.core.resources.api.StandardHomeDirectoryConstants.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Builds a resource located in a standard home directory.  The resource will
 * have a single version with a name based on the last path of the home
 * directory.  This version will have some or all of the following properties:
 * 
 * <ul>
 *     <li>&lt;name:uppercased&gt;_HOME - pointing to the home directory</li>
 *     <li>&lt;name&gt;.bin.dir - pointing to the bin subdirectory</li>
 *     <li>&lt;name&gt;.bin - pointing to the tool binary (or script)</li>
 *     <li>&lt;name&gt;.lib - pointing to the lib subdirectory (if any)</li>
 * </ul>
 */
public class StandardHomeDirectoryResourceBuilder implements FileSystemResourceBuilder
{
    private static final Logger LOG = Logger.getLogger(StandardHomeDirectoryResourceBuilder.class);

    private String resourceName;
    private String binaryName;
    private boolean script;

    /**
     * Creates a simple builder where the resource and binary name match, and
     * the binary is a script.
     * 
     * @param resourceName name of the resource to create
     */
    public StandardHomeDirectoryResourceBuilder(String resourceName)
    {
        this(resourceName, resourceName, true);
    }

    /**
     * Creates a builder that will build a resource of the given name, with the
     * specified binary file.
     * 
     * @param resourceName name of the resource to create
     * @param binaryName   name of the binary for the tool
     * @param script       true if the binary is a script, false if it is a
     *                     true binary
     */
    public StandardHomeDirectoryResourceBuilder(String resourceName, String binaryName, boolean script)
    {
        this.resourceName = resourceName;
        this.binaryName = binaryName;
        this.script = script;
    }

    public ResourceConfiguration buildResource(File home)
    {
        try
        {
            ResourceConfiguration resource = new ResourceConfiguration(resourceName);

            // What is the version of this installation? Use the name of the home directory for this.
            // TODO I think we can seriously improve on this, it is not very
            // TODO accurate in many common setups.
            ResourceVersionConfiguration version = new ResourceVersionConfiguration(home.getName());
            resource.addVersion(version);
            resource.setDefaultVersion(version.getValue());

            version.addProperty(new ResourcePropertyConfiguration(convertResourceNameToEnvironmentVariable(resourceName), getNormalisedPath(home), true, false, false));

            // configure the binary directory for this version.
            String binDirPath = getNormalisedPath(getBinaryDirectory(home));
            version.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY_DIRECTORY, binDirPath, false, true, false));

            String binaryPath = getNormalisedPath(getBinaryFile(home, binaryName, script));
            version.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY, binaryPath, false, false, false));

            File lib = getLibraryDirectory(home);
            if (lib.isDirectory())
            {
                String libDirPath = FileSystemUtils.normaliseSeparators(lib.getAbsolutePath());
                version.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_LIBRARY_DIRECTORY, libDirPath, false, false, false));
            }

            return resource;
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
