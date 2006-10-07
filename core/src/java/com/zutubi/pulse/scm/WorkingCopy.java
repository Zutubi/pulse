package com.zutubi.pulse.scm;

import com.zutubi.pulse.personal.PersonalBuildWorker;

import java.util.Properties;

/**
 */
public interface WorkingCopy extends PersonalBuildWorker
{
    boolean matchesRepository(Properties repositoryDetails) throws SCMException;

    WorkingCopyStatus getStatus() throws SCMException;

    void update() throws SCMException;
}
