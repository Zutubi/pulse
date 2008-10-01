package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.personal.PersonalBuildWorker;

/**
 */
public interface WorkingCopy extends PersonalBuildWorker
{
    boolean matchesLocation(String location) throws ScmException;

    WorkingCopyStatus getLocalStatus(String... spec) throws ScmException;

    Revision update() throws ScmException;
}
