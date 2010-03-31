package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a capture of a single file from the local file sysytem.
 *
 * @see FileArtifact
 */
@SymbolicName("zutubi.fileArtifactConfig")
@Form(fieldOrder = {"name", "file", "featured", "postProcessors", "calculateHash", "hashAlgorithm", "type", "failIfNotPresent", "ignoreStale", "publish", "artifactPattern"})
public class FileArtifactConfiguration extends FileSystemArtifactConfigurationSupport
{
    @Required
    private String file;

    public FileArtifactConfiguration()
    {
    }

    public FileArtifactConfiguration(String name, String file)
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

    public Class<? extends Artifact> artifactType()
    {
        return FileArtifact.class;
    }
}
