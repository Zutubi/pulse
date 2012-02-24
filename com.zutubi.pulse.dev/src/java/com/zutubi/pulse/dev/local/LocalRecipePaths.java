package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.RecipePaths;

import java.io.File;

/**
 * The implementation of the RecipePaths interface for the local build. The output
 * path is relative to the base directory unless an absolute directory is specified.
 */
public class LocalRecipePaths implements RecipePaths
{
    private File baseDir;
    private File outputDir;

    public LocalRecipePaths(File base, String output)
    {
        baseDir = base;
        outputDir = new File(output);
        if (!outputDir.isAbsolute())
        {
            outputDir = new File(base, output);
        }

    }

    public File getCheckoutDir()
    {
        return null;
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public File getOutputDir()
    {
        return outputDir;
    }
}
