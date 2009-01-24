package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 */
@SymbolicName("zutubi.fileOutputConfig")
public class FileOutputConfiguration extends FileSystemOutputConfigurationSupport
{
    @Required
    private String file;

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public Output createOutput()
    {
        return buildOutput(FileOutput.class);
    }
}
