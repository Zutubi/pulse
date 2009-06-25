package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.Revision;

/**
 * Wraps a repository revision with extra possibilities for personal builds.
 */
public class PersonalBuildRevision
{
    private Revision revision;
    private boolean updateSupported;

    public PersonalBuildRevision(Revision revision, boolean updateSupported)
    {
        this.revision = revision;
        this.updateSupported = updateSupported;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public boolean isUpdateSupported()
    {
        return updateSupported;
    }
}
