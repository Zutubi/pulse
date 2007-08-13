package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PersonalBuildWorker;

import java.util.Properties;

/**
 */
public interface WorkingCopy extends PersonalBuildWorker
{
    boolean matchesRepository(Properties repositoryDetails) throws ScmException;

    WorkingCopyStatus getLocalStatus(String... spec) throws ScmException;

    Revision update() throws ScmException;
}
