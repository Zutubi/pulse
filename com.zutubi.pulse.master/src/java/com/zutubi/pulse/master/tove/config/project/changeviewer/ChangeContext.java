package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 * Context of a changelist to retrieve information about.  Convenience methods
 * are also supplied for the common operation of getting previous revisions.
 */
public interface ChangeContext
{
    /**
     * @return the revision of the changelist that this operation relates to
     */
    Revision getRevision();

    /**
     * @return the configuration of the SCM implementation that created the
     *         changelist
     */
    ScmConfiguration getScmConfiguration();

    /**
     * @return a client for the SCM implementation that created the changelist
     */
    ScmClient getScmClient();

    /**
     * @return an SCM context that may be passed to the SCM client if required
     */
    ScmContext getScmContext();

    /**
     * Returns the revision previous to the changelist's revision, if any.
     *
     * @return the previous changelist revision, or null if there is none
     * @throws ScmException if the SCM implementation encounters an error
     */
    Revision getPreviousChangelistRevision() throws ScmException;

    /**
     * Returns the file revision previous to the given file's revision, if any.
     *
     * @param fileChange the file change to retrieve the revision from
     * @return the previous file revision, or null if there is none
     * @throws ScmException if the SCM implementation encounters an error
     */
    Revision getPreviousFileRevision(FileChange fileChange) throws ScmException;
}
