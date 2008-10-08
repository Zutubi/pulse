package com.zutubi.pulse.core.scm.api;

/**
 */
public interface WorkingCopy
{
    boolean matchesLocation(String location) throws ScmException;

    WorkingCopyStatus getLocalStatus(String... spec) throws ScmException;

    Revision update() throws ScmException;
}
