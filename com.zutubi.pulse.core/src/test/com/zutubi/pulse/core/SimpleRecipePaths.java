package com.zutubi.pulse.core;

import java.io.File;

/**
 * A simple {@link RecipePaths} implementation for testing.
 */
public class SimpleRecipePaths implements RecipePaths
{
    private File baseDir;
    private File outputDir;

    public SimpleRecipePaths(File baseDir, File outputDir)
    {
        this.baseDir = baseDir;
        this.outputDir = outputDir;
    }

    public File getCheckoutDir()
    {
        return baseDir;
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
