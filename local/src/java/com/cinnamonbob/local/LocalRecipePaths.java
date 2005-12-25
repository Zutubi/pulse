package com.cinnamonbob.local;

import com.cinnamonbob.core.RecipePaths;

import java.io.File;

/**
 * The implementation of the RecipePaths interface for the local build. The output
 * path is relative to the working directory unless an absolute directory is specified.
 *
 */
public class LocalRecipePaths implements RecipePaths
{
    private File workDir;
    private File outputDir;

    public LocalRecipePaths(File work, String output)
    {
        workDir = work;
        outputDir = new File(output);
        if (!outputDir.isAbsolute())
        {
            outputDir = new File(work, output);
        }

    }

    public File getWorkDir()
    {
        return workDir;
    }

    public File getOutputDir()
    {
        return outputDir;
    }
}
