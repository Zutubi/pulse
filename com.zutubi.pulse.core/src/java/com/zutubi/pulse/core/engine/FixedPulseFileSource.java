package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;

/**
 * "Sources" pulse files from a fixed string.  Used for projects that have a file generated entirely
 * from their internal configuration.  The file is generated and cached directly in this source.
 */
public class FixedPulseFileSource implements PulseFileSource
{
    private String fileContent;

    public FixedPulseFileSource(String fileContent)
    {
        this.fileContent = fileContent;
    }

    public String getPath()
    {
        return null;
    }

    public String getFileContent(FileResolver resolver) throws Exception
    {
        return fileContent;
    }
}
