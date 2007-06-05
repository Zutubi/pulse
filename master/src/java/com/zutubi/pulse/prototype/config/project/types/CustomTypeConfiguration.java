package com.zutubi.pulse.prototype.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.TextArea;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;

/**
 *
 *
 */
@SymbolicName("internal.customTypeConfig")
public class CustomTypeConfiguration extends TypeConfiguration
{
    @TextArea
    private String pulseFileString;

    public String getPulseFile(long id, ProjectConfiguration projectConfig, Revision revision, PatchArchive patch)
    {
        return pulseFileString;
    }

    public String getPulseFileString()
    {
        return pulseFileString;
    }

    public void setPulseFileString(String pulseFileString)
    {
        this.pulseFileString = pulseFileString;
    }
}
