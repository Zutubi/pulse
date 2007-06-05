package com.zutubi.pulse.prototype.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.personal.PatchArchive;

/**
 *
 *
 */
@SymbolicName("internal.versionedTypeConfig")
public class VersionedTypeConfiguration extends TypeConfiguration
{
    private String pulseFileName;

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public String getPulseFile(long id, ProjectConfiguration projectConfig, Revision revision, PatchArchive patch)
    {
        return null;
    }
}
