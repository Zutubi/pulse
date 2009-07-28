package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;

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
