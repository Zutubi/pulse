package com.zutubi.pulse.core;

import java.io.File;
import java.io.OutputStream;

/**
 * Information passed to all commands as they execute, allowing the command
 * access to it's execution context (or environment).
 */
public class CommandContext
{
    private RecipePaths paths;
    private OutputStream outputStream;
    private File outputDir;

    public CommandContext(RecipePaths paths, File outputDir)
    {
        this.paths = paths;
        this.outputDir = outputDir;
    }

    public RecipePaths getPaths()
    {
        return paths;
    }

    public File getOutputDir()
    {
        return outputDir;
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
