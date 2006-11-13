package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Revision;

/**
 * @deprecated This entity is no longer required within the schema. It is the middle man in a one to one mapping
 * between a build result and the associated revision.
 */
public class BuildScmDetails extends Entity
{
    private Revision revision;

    public BuildScmDetails()
    {
    }

    public BuildScmDetails(Revision revision)
    {
        this.revision = revision;
    }

    public Revision getRevision()
    {
        return revision;
    }

    private void setRevision(Revision revision)
    {
        this.revision = revision;
    }
}
