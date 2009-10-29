package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;

import java.io.File;

/**
 * Provides pulse files from a fixed string.  Used for projects that have a file generated entirely
 * from their internal configuration.  The file is generated and cached directly in this provider.
 */
public class FixedPulseFileProvider implements PulseFileProvider
{
    private String fileContent;

    public FixedPulseFileProvider(String fileContent)
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

    public File getImportRoot()
    {
        return null;
    }
}
