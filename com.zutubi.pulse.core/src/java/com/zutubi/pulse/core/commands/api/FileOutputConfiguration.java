package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a capture of a single file from the local file sysytem.
 *
 * @see FileOutput
 */
@SymbolicName("zutubi.fileOutputConfig")
@Form(fieldOrder = {"name", "file", "postProcessors", "type", "failIfNotPresent", "ignoreStale"})
public class FileOutputConfiguration extends FileSystemOutputConfigurationSupport
{
    @Required
    private String file;

    public FileOutputConfiguration()
    {
    }

    public FileOutputConfiguration(String name, String file)
    {
        super(name);
        this.file = file;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public Class<? extends Output> outputType()
    {
        return FileOutput.class;
    }
}
