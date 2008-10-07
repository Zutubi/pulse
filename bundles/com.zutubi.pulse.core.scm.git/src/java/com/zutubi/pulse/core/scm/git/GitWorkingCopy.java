package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.*;

/**
 * to be implemented.
 */
public class GitWorkingCopy implements WorkingCopy
{
    public boolean matchesLocation(String location) throws ScmException
    {
        return false;
    }

    public WorkingCopyStatus getLocalStatus(String... spec) throws ScmException
    {
        return null;
    }

    public Revision update() throws ScmException
    {
        return null;
    }

    public void setUI(PersonalBuildUI ui)
    {

    }
}
