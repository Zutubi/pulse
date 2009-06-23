package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A test utilities class that provides access to the internal artifact
 * repository during acceptance testing.
 */
public class ArtifactRepositoryTestUtils
{
    /**
     * Wait for at most the timeout number of milliseconds for a file to exist within
     * the repository.  If the file does not exist by the end of the timeout, false is
     * returned.
     *
     * @param path      to the file of interest, relative to the base of the artifact repository.
     * @param timeout   the timeout in milliseconds
     * @return  true if the file exists, false otherwise
     * @throws IOException is thrown on error.
     */
    public static boolean waitUntilInRepository(String path, int timeout) throws IOException
    {
        File artifact = new File(getArtifactRepository(), path);
        try
        {
            long startTime = System.currentTimeMillis();
            while (!artifact.exists() && System.currentTimeMillis() - startTime < timeout)
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

    /**
     * Return the path (relative to the root of the artifact repository) of an ivy file
     * for the given project build.
     *
     * @param projectName   the name of the ivy file's project
     * @param buildNumber   the build number for the ivy file's build
     * @return  the path, relative to the root of the artifact repository, for the ivy
     * file in question.
     */
    public static String ivyPath(String projectName, int buildNumber)
    {
        return ivyPath(projectName, Integer.toString(buildNumber));
    }

    public static String ivyPath(String projectName, String version)
    {
        return projectName + "/ivy-" + version + ".xml";
    }

    /**
     * Return the contents of an ivy file for the given project build.
     *
     * @param projectName   the name of the ivy file's project
     * @param buildNumber   the build number for the ivy file's build
     * @return  the content of the ivy file.
     * @throws IOException if the ivy file does not exist or an error is encountered
     * retrieving the file content.
     */
    public static String getIvyFile(String projectName, int buildNumber) throws IOException
    {
        return getIvyFile(projectName, String.valueOf(buildNumber));
    }

    public static String getIvyFile(String projectName, String version) throws IOException
    {
        String path = ivyPath(projectName, version);
        File ivyFile = new File(getArtifactRepository(), path);
        return IOUtils.fileToString(ivyFile);
    }

    /**
     * Returns true if the specified path references a file that exists within the
     * artifact repository.
     * @param path  the path identifying the file in the artifact repository, relative to
     * the base of the repository.
     * @return  true if a file exists, false otherwise.
     * @throws IOException on error.
     */
    public static boolean isInArtifactRepository(String path) throws IOException
    {
        return new File(getArtifactRepository(), path).exists();
    }

    /**
     * Returns true if the specified path does not reference a file within the artifact
     * repository.
     * @param path  the path identifying the location in the artifact repository, relative
     * to the base of the repository.
     * @return  true if no file exists at the specified path, false otherwise.
     * @throws IOException on error
     * @see #isInArtifactRepository(String)
     */
    public static boolean isNotInArtifactRepository(String path) throws IOException
    {
        return !isInArtifactRepository(path);
    }

    /**
     * Clear out the contents of the artifact repository.
     *
     * @throws IOException on error.
     */
    public static void clearArtifactRepository() throws IOException
    {
        File repository = getArtifactRepository();
        if (repository.isDirectory())
        {
            if (!FileSystemUtils.rmdir(repository))
            {
                throw new IOException("Failed to remove dir: " + repository.getCanonicalPath());
            }
        }
        if (!repository.mkdirs())
        {
            throw new IOException("Failed to create dir: " + repository.getCanonicalPath());
        }
    }

    /**
     * Create an empty file in the artifact repository at the specified path.
     * @param path  the path for the new file, relative to the root of the artifact
     * repository.
     * @throws IOException on error.
     */
    public static void createArtifactFile(String path) throws IOException
    {
        File file = new File(getArtifactRepository(), path);
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
     * Get the root of the artifact repository.
     * @return  the root directory of the artifact repository.
     * @throws IOException on error.
     */
    public static File getArtifactRepository() throws IOException
    {
        return new File(AcceptanceTestUtils.getDataDirectory(), "repository");
    }

    /**
     * Get the contents of the specified attribute from the ivy xml file.
     *
     * @param attributeName     name of the field whose data is being retrieved.
     * @param ivyFile           reference to the ivy xml file
     *
     * @return  the data, or null if the field was not located.
     *
     * @throws IOException on error
     */
    public static String getAttribute(String attributeName, File ivyFile) throws IOException
    {
        String content = IOUtils.fileToString(ivyFile);
        Pattern pattern = Pattern.compile(".*"+attributeName+"=\"(.*)\".*", Pattern.DOTALL);
        Matcher m = pattern.matcher(content);
        if (m.matches())
        {
            return m.group(1);
        }
        return null;
    }
}
