package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.File;
import java.io.OutputStream;

/**
 * Information passed to all commands as they execute, allowing the command
 * access to it's execution context (or environment).
 */
public class CommandContext
{
    /**
     * Paths for the recipe being executed.
     */
    private RecipePaths paths;
    /**
     * Output directory for the command being executed.
     */
    private File outputDir;
    /**
     * Test results being accumulated for the recipe.
     */
    private TestSuiteResult testResults;
    /**
     * If not null, stream to write command output to (in addition to any output artifact).
     */
    private OutputStream outputStream;

    public CommandContext(RecipePaths paths, File outputDir, TestSuiteResult testResults)
    {
        this.paths = paths;
        this.outputDir = outputDir;
        this.testResults = testResults;
    }

    public RecipePaths getPaths()
    {
        return paths;
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public TestSuiteResult getTestResults()
    {
        return testResults;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }
}
