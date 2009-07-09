package com.zutubi.pulse.core.scm.api;

/**
 * Capabilities that are used to indicate which optional methods an
 * implementation of {@link WorkingCopy} supports.  This allows implementations
 * to be staged, and/or functionality that makes no sense for a particular SCM
 * to be disabled.
 */
public enum WorkingCopyCapability
{
    REMOTE_REVISION,
    LOCAL_REVISION,
    UPDATE
}
