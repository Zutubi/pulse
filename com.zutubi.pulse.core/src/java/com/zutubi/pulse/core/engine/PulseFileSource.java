package com.zutubi.pulse.core.engine;

/**
 * The source of a pulse file, primarily the file contents but possibly also
 * other information.
 */
public class PulseFileSource
{
    private String path;
    private String fileContent;

    public PulseFileSource(String fileContent)
    {
        this.fileContent = fileContent;
    }

    public PulseFileSource(String path, String fileContent)
    {
        this.path = path;
        this.fileContent = fileContent;
    }

    public String getPath()
    {
        return path;
    }

    public String getFileContent()
    {
        return fileContent;
    }
}
