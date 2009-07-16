package com.zutubi.pulse.core.scm.api;

/**
 * Capabilities that are used to indicate which optional methods an
 * implementation of {@link WorkingCopy} supports.  This allows implementations
 * to be staged, and/or functionality that makes no sense for a particular SCM
 * to be disabled.
 */
public enum WorkingCopyCapability
{
    /**
     * The working copy can retrieve the latest revision for a project from the
     * remote repository.
     */
    REMOTE_REVISION,
    /**
     * The working copy can guess the revision of the local working copy (i.e
     * the revision of the last update).
     */
    LOCAL_REVISION,
    /**
     * The working copy supports updating to a revision.
     */
    UPDATE
}
