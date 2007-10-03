package com.zutubi.pulse.prototype.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

/**
 *
 *
 */
@SymbolicName("zutubi.typeConfig")
public abstract class TypeConfiguration extends AbstractConfiguration
{
    public abstract String getPulseFile(long id, ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception;
}
