package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.IvyPatternHelper;

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

    private File base;

    private String ivyPattern = "([organisation]/)[module]/([stage]/)ivy-[revision].xml";
    private String artifactPattern = "([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[type]";

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

    /**
     * Create a new instance of the repository, using the specified directory
     * as the base directory for this repository.
     *
     * @param dir  the repositories base directory.
     */
    public Repository(File dir)
    {
        this.base = dir;
    }

    /**
     * Clear out the contents of the repository.
     *
     * @throws IOException on error.
     */
    public void clear() throws IOException
    {
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
        File artifact = new File(base, path);
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

    public IvyFile getIvyFile(String name, Object revision)
    {
        return getIvyFile(null, name, revision);
    }
    
    /**
     * Get a reference to the ivy file for the specified project and revision.  Note that
     * the reference points to where the ivy file would be if it exists in the repository,
     * and does not infer that it actually does exist.  For that, use
     * {@link com.zutubi.pulse.acceptance.dependencies.IvyFile#exists()}
     *
     * @param org       the projects organisation, or null if no organisation is present
     * @param name      the name of the project
     * @param revision  the revision of the ivy file to be retrieved
     *
     * @return  a reference to the ivy file.
     */
    public IvyFile getIvyFile(String org, String name, Object revision)
    {
        String revisionString = (revision != null) ? revision.toString() : null;
        String orgString = (org != null) ? org : "";
        ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(orgString, name, null, revisionString);
        String path = IvyPatternHelper.substitute(ivyPattern, mrid);
        return new IvyFile(this, path);
    }

    public ArtifactFile getArtifactFile(String name, String stageName, Object revision, String artifactName, String artifactExtension)
    {
        return getArtifactFile(null, name, stageName, revision, artifactName,  artifactExtension);
    }

    public ArtifactFile getArtifactFile(String org, String name, String stageName, Object revision, String artifactName, String artifactExtension)
    {
        String revisionString = (revision != null) ? revision.toString() : null;
        String orgString = (org != null) ? org : "";
        Map<String, String> extraAttributes = new HashMap<String, String>();
        extraAttributes.put("e:stage", IvyUtils.ivyEncodeStageName(stageName));
        ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(orgString, name, revisionString, extraAttributes);

        String path = IvyPatternHelper.substitute(artifactPattern, mrid, artifactName, artifactExtension, artifactExtension);
        return new ArtifactFile(this, path);
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
        return new File(base, path).exists();
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
     * @throws IOException on error.
     */
    protected boolean createFile(String path) throws IOException
    {
        File file = new File(base, path);
        File parentFile = file.getParentFile();
        if (!parentFile.mkdirs())
        {
            throw new IOException("Failed to create directory: " + parentFile.getCanonicalPath());
        }
        if (!file.createNewFile())
        {
            throw new IOException("Failed to create file: " + file.getCanonicalPath());
        }
        return true;
    }

    /**
     * Get the base directory being used by this repository instance
     *
     * @return the repositories base directory.
     */
    protected File getBase()
    {
        return base;
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
