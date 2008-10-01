package com.zutubi.pulse.core.resources;

import static com.zutubi.pulse.core.resources.StandardHomeDirectoryConstants.getBinaryFile;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.List;

/**
 */
public class StandardHomeDirectoryFileLocator implements FileLocator
{
    private FileLocator delegate;

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
