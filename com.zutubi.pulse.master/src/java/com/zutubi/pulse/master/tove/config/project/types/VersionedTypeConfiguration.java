package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.ExternalPulseFileProvider;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmDirAction;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.FileSystemUtils;

/**
 * Pulse file project where the pulse file is stored in the project's SCM.
 */
@SymbolicName("zutubi.versionedTypeConfig")
@Wire
public class VersionedTypeConfiguration extends TypeConfiguration
{
    @BrowseScmDirAction
    private String pulseFileName;

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public PulseFileProvider getPulseFile() throws Exception
    {
        String normalisedPath = FileSystemUtils.normaliseSeparators(pulseFileName);
        return new ExternalPulseFileProvider(normalisedPath);
    }
}
