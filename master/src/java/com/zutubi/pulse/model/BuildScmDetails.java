package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Revision;

/**
 * Holds build model information for single SCM, e.g. changes committed to
 * that SCM since the last build.
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
