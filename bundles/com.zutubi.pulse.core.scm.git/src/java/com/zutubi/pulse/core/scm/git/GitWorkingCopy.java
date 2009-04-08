package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;

import java.io.File;

/**
 * to be implemented.
 */
public class GitWorkingCopy implements WorkingCopy
{
    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        return false;
    }

    public boolean writePatchFile(WorkingCopyContext context, File patchFile, String... spec) throws ScmException
    {
        throw new RuntimeException("Not implemented");
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        return null;
    }
}
