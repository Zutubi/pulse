package com.zutubi.pulse.prototype.config.types;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.typeConfig")
public abstract class TypeConfiguration extends AbstractConfiguration
{
    public abstract String getPulseFile(long id, ProjectConfiguration projectConfig, Revision revision, PatchArchive patch);
}
