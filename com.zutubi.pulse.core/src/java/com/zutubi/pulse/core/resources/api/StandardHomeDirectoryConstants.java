package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.SystemUtils;

import java.io.File;

/**
 * Constants and conventions used by the {@link StandardHomeDirectoryResourceLocator}
 * and the implementations it relies upon.
 */
class StandardHomeDirectoryConstants
{
    public static final String EXTENSION_SCRIPT     = ".bat";
    public static final String EXTENSION_EXECUTABLE = ".exe";

    public static final String DIRECTORY_BINARY = "bin";
    public static final String DIRECTORY_LIBRARY = "lib";

    public static String convertResourceNameToEnvironmentVariable(String resourceName)
    {
        return resourceName.toUpperCase() + "_HOME";
    }

    public static File getBinaryDirectory(File home)
    {
        return new File(home, DIRECTORY_BINARY);
    }

    public static File getLibraryDirectory(File home)
    {
        return new File(home, DIRECTORY_LIBRARY);
    }

    public static File getBinaryFile(File home, String binaryName, boolean script)
    {
        return new File(getBinaryDirectory(home), getSystemBinaryName(binaryName, script));
    }

    public static String getSystemBinaryName(String binaryName, boolean script)
    {
        if (SystemUtils.IS_WINDOWS)
        {
            if (script)
            {
                binaryName += EXTENSION_SCRIPT;
            }
            else
            {
                binaryName += EXTENSION_EXECUTABLE;
            }
        }

        return binaryName;
    }
}
