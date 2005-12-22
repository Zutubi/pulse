package com.cinnamonbob.local;

import com.cinnamonbob.core.RecipePaths;

import java.io.File;

/**
 */
public class LocalRecipePaths implements RecipePaths
{
    private File workDir;
    private File outputDir;

    public LocalRecipePaths(File workDir, String outputDir)
    {
        this.workDir = workDir;
        this.outputDir = new File(workDir, outputDir);
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
