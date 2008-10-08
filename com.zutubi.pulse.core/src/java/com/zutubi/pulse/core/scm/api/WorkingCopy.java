package com.zutubi.pulse.core.scm.api;

/**
 * An interface for interaction with a checked-out working copy of a project.
 * This interface must be implemented to support personal builds for an SCM.
 */
public interface WorkingCopy
{
    boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException;

    WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... spec) throws ScmException;

    Revision update(WorkingCopyContext context, Revision revision) throws ScmException;
}
