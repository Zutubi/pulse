package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.ExternalPulseFileSource;
import com.zutubi.pulse.core.engine.PulseFileSource;
import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.validation.annotations.Required;

/**
 * Pulse file project where the pulse file is stored in the project's SCM.
 */
@SymbolicName("zutubi.versionedTypeConfig")
@Wire
public class VersionedTypeConfiguration extends TypeConfiguration
{
    @Required
    @FieldAction(template = "actions/browse-scm-file")
    private String pulseFileName;

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public PulseFileSource getPulseFile() throws Exception
    {
        String normalisedPath = FileSystemUtils.normaliseSeparators(pulseFileName);
        return new ExternalPulseFileSource(normalisedPath);
    }
}
