package com.zutubi.pulse.core.engine;

import java.io.File;

/**
 * The source of a pulse file, primarily the file contents but possibly also
 * other information.
 */
public class PulseFileSource
{
    private String path;
    private String fileContent;
    private File importRoot;

    public PulseFileSource(String fileContent)
    {
        this(null, fileContent);
    }

    public PulseFileSource(String path, String fileContent)
    {
        this(path, fileContent, null);
    }

    public PulseFileSource(String path, String fileContent, File importRoot)
    {
        this.path = path;
        this.fileContent = fileContent;
        this.importRoot = importRoot;
    }

    public String getPath()
    {
        return path;
    }

    public String getFileContent()
    {
        return fileContent;
    }

    public File getImportRoot()
    {
        return importRoot;
    }
}
