package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.*;

/**
 * to be implemented.
 */
public class GitWorkingCopy implements WorkingCopy
{
    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        return false;
    }

    public WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... spec) throws ScmException
    {
        return null;
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        return null;
    }
}
