package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Defines what the pulse file looks like for a project.  It could be built
 * from a restricted template (e.g. an ant project) or could be had-written
 * by the user (e.g. a custom project) or anything else that can produce a
 * valid pulse file.
 */
@SymbolicName("zutubi.typeConfig")
public abstract class TypeConfiguration extends AbstractConfiguration
{
    public abstract String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception;
}
