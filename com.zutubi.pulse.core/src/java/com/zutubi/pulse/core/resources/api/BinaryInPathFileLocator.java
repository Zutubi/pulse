package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.SystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A file locator that looks for a named binary in the search PATH.  If the
 * binary is in the PATH it is returned as the single result from
 * {@link #locate()}.
 */
public class BinaryInPathFileLocator implements FileLocator
{
    private String binaryName;

    /**
     * Creates a locator that will search for the binary of the given name.
     * 
     * @param binaryName name of the binary to search for
     */
    public BinaryInPathFileLocator(String binaryName)
    {
        this.binaryName = binaryName;
    }

    public List<File> locate()
    {
        File binaryFile = SystemUtils.findInPath(binaryName);
        if (binaryFile == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(binaryFile);
        }
    }
}
