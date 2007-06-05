package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.Properties;

/**
 */
public abstract class PulseFileDetails extends Entity
{
    public abstract PulseFileDetails copy();

    public abstract boolean isBuiltIn();

    public abstract String getType();

    public abstract Properties getProperties();

    /**
     * Returns the Pulse file for the given project at the given revision.
     * If the patch given is not null, any changes from the patch that affect
     * the pulse file should also be taken into account.
     *
     * @param id            unique id for the request
     * @param projectConfig configuration for the project to return the pulse
     *                      file for
     * @param project       the project to return the pulse file for
     * @param revision      the revision to return the pulse file for
     * @param patch         if not null, a patch which may possibly carry
     *                      information that alters the pulse file
     * @return the pulse file at the given revision
     */
    public abstract String getPulseFile(long id, ProjectConfiguration projectConfig, Project project, Revision revision, PatchArchive patch);

}
