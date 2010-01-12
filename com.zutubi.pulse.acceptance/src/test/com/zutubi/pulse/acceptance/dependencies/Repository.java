package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyEncoder;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleRevisionId;
import com.zutubi.util.FileSystemUtils;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor.EXTRA_ATTRIBUTE_STAGE;

/**
 * The repository class provides tests with a simple way to interact with the
 * embedded artifact repository, hiding away details about paths and ivy file
 * formats. 
 */
public class Repository
{
    /**
     * Timeout waiting for an artifact to appear in the repository.
     */
    private static final int AVAILABILITY_TIMEOUT = 5000;

    private IvyConfiguration configuration;

    /**
     * Create a new instance of the repository, using the acceptance tests standard
     * repository base path.
     *
     * @throws IOException on error
     */
    public Repository() throws IOException
    {
        this(getRepositoryBase());
    }

    public Repository(File dir)
    {
        this.configuration = new IvyConfiguration(dir.toString());
    }

    public Repository(IvyConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Clear out the contents of the repository.
     *
     * @throws IOException on error.
     */
    public void clean() throws IOException
    {
        File base = getBase();
        
        if (base.isDirectory())
        {
            if (!FileSystemUtils.rmdir(base))
            {
                throw new IOException("Failed to remove dir: " + base.getCanonicalPath());
            }
        }
        if (!base.mkdirs())
        {
            throw new IOException("Failed to create dir: " + base.getCanonicalPath());
        }
    }

    /**
     * Wait until the specified path exists within the repository, or until the embedded timeout is reached.
     *
     * @param path  that we are waiting for
     * @return  true if the path exists, false if we reach the timeout and the path still does not exist.
     *
     * @throws IOException  on error
     */
    public boolean waitUntilInRepository(String path) throws IOException
    {
        File artifact = new File(getBase(), path);
        try
        {
            long startTime = System.currentTimeMillis();
            while (!artifact.exists() && System.currentTimeMillis() - startTime < AVAILABILITY_TIMEOUT)
            {
                Thread.sleep(200);
            }
        }
        catch (InterruptedException e)
        {
            // noop.
        }
        return artifact.exists();
    }

    public IvyModuleDescriptor getIvyModuleDescriptor(String name, Object revision) throws Exception
    {
        return getIvyModuleDescriptor(null, name, revision);
    }
    
    /**
     * Get a handle to the ivy module descriptor for the specified project and revision.
     *
     * @param org       the projects organisation, or null if no organisation is present
     * @param name      the name of the project
     * @param revision  the revision of the ivy descriptor to be retrieved
     *
     * @return  a reference to the ivy descriptor
     *
     * @throws Exception on error
     */
    public IvyModuleDescriptor getIvyModuleDescriptor(String org, String name, Object revision) throws Exception
    {
        return IvyModuleDescriptor.newInstance(new File(getBase(), getIvyModuleDescriptorPath(org, name, revision)), configuration);
    }

    public String getIvyModuleDescriptorPath(String org, String name, Object revision) throws Exception
    {
        String revisionString = (revision != null) ? revision.toString() : null;
        String orgString = (org != null) ? org : "";
        ModuleRevisionId mrid = IvyEncoder.encode(IvyModuleRevisionId.newInstance(orgString, name, null, revisionString));

        return configuration.getIvyPath(mrid);
    }

    public String getArtifactPath(String name, String stageName, Object revision, String artifactName, String artifactExtension)
    {
        return getArtifactPath(null, name, stageName, revision, artifactName,  artifactExtension);
    }

    public String getArtifactPath(String org, String name, String stageName, Object revision, String artifactName, String artifactExtension)
    {
        String revisionString = (revision != null) ? revision.toString() : null;
        String orgString = (org != null) ? org : "";
        Map<String, String> extraAttributes = new HashMap<String, String>();
        extraAttributes.put(EXTRA_ATTRIBUTE_STAGE, stageName);
        ModuleRevisionId mrid = IvyEncoder.encode(IvyModuleRevisionId.newInstance(orgString, name, revisionString, extraAttributes));

        return configuration.getArtifactPath(mrid, artifactName, artifactExtension);
    }

    /**
     * Returns true if the specified path references a file that exists within the
     * artifact repository.
     * @param path  the path identifying the file in the artifact repository, relative to
     * the base of the repository.
     * @return  true if a file exists, false otherwise.
     * @throws IOException on error.
     */
    public boolean isInRepository(String path) throws IOException
    {
        return new File(getBase(), path).exists();
    }

    /**
     * Returns true if the specified path does not reference a file within the artifact
     * repository.
     * @param path  the path identifying the location in the artifact repository, relative
     * to the base of the repository.
     * @return  true if no file exists at the specified path, false otherwise.
     * @throws IOException on error
     * @see #isInRepository(String)
     */
    public boolean isNotInRepository(String path) throws IOException
    {
        return !isInRepository(path);
    }

    /**
     * Create an empty file in the artifact repository at the specified path.
     * @param path  the path for the new file, relative to the root of the artifact
     * repository.
     *
     * @throws IOException on error.
     */
    protected void createFile(String path) throws IOException
    {
        File file = new File(getBase(), path);
        File parentFile = file.getParentFile();
        if (!parentFile.mkdirs())
        {
            throw new IOException("Failed to create directory: " + parentFile.getCanonicalPath());
        }
        if (!file.createNewFile())
        {
            throw new IOException("Failed to create file: " + file.getCanonicalPath());
        }
    }

    /**
     * Get the base directory being used by this repository instance
     *
     * @return the repositories base directory.
     */
    protected File getBase()
    {
        return new File(configuration.getRepositoryBase());
    }

    /**
     * Get the root of the artifact repository.
     * @return  the root directory of the artifact repository.
     * @throws IOException on error.
     */
    public static File getRepositoryBase() throws IOException
    {
        return new File(AcceptanceTestUtils.getDataDirectory(), "repository");
    }
}
