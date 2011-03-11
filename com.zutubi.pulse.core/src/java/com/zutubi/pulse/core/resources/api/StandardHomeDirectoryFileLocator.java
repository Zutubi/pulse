package com.zutubi.pulse.core.resources.api;

import static com.zutubi.pulse.core.resources.api.StandardHomeDirectoryConstants.getBinaryFile;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.List;

/**
 * A locator that looks for home directories matching the expectations of
 * {@link StandardHomeDirectoryResourceLocator}.  The home directory path is
 * defined in an environment variable, and within it the locator expects to
 * find a bin directory with a binary (or script) of a given name.  If the
 * environment variable does not exist or does not point to a directory with
 * such a binary, no results are returned.
 */
public class StandardHomeDirectoryFileLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that looks for a tool installed in a home directory
     * specified by a given environment variable.
     * 
     * @param environmentVariable name of the environment variable that defines
     *                            the home directory
     * @param binaryName          name of the binary expected in the bin/
     *                            subdirectory
     * @param script              if true, the binary is a script, if false it
     *                            is an executable (used to determine the
     *                            suffix on Windows)
     */
    public StandardHomeDirectoryFileLocator(String environmentVariable, final String binaryName, final boolean script)
    {
        delegate = new FilteringFileLocator(new HomeDirectoryFileLocator(environmentVariable), new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return getBinaryFile(file, binaryName, script).isFile();
            }
        });
    }

    public List<File> locate()
    {
        return delegate.locate();
    }
}
