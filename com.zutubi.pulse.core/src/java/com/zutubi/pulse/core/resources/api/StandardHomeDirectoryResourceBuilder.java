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

            String binDirPath = getNormalisedPath(getBinaryDirectory(home));
            String binaryPath = getNormalisedPath(getBinaryFile(home, binaryName, script));

            String versionName = getVersionName(home, new File(binaryPath));
            ResourceVersionConfiguration version = new ResourceVersionConfiguration(versionName);
            resource.addVersion(version);
            resource.setDefaultVersion(versionName);

            version.addProperty(new ResourcePropertyConfiguration(convertResourceNameToEnvironmentVariable(resourceName), getNormalisedPath(home), true, false, false));
            version.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY_DIRECTORY, binDirPath, false, true, false));
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

    /**
     * Determines the version of the discovered resource.  By default, the
     * basename of the home directory is used.  Where possible, subclasses
     * should override this for more accurate behaviour.
     * 
     * @param homeDir the home directory
     * @param binary  the binary file
     * @return name to use for the resource version
     */
    protected String getVersionName(File homeDir, File binary)
    {
        return homeDir.getName();
    }
}
