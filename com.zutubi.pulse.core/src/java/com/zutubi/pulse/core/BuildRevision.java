package com.zutubi.pulse.core;

import com.zutubi.pulse.core.scm.api.Revision;

/**
 * Represents the revision to be built.  The revision of the build can be initialised
 * at any time before the build starts, but once initialised, it can not be changed.
 */
public class BuildRevision
{
    /**
     * The revision to build, which may be null if it has not yet been
     * initialised.
     */
    private Revision revision;
    /**
     * True if this is a revision explicitly specified by the user, when
     * triggering the build.
     */
    private boolean user = false;

    /**
     * Construct a new revision that will be determined lazily.
     */
    public BuildRevision()
    {
    }

    /**
     * Create a new revision that will stay fixed at the given revision.
     *
     * @param revision  the revision to build, which will not change.
     * @param user      if true, this is a user-specified revision (as
     *                  opposed to a fixed revision decided on by Pulse
     *                  itself, as happens e.g. when isolating changes).
     */
    public BuildRevision(Revision revision, boolean user)
    {
        if (revision == null)
        {
            throw new NullPointerException("Revision may not be null");
        }

        this.revision = revision;
        this.user = user;
    }

    /**
     * @return the underlying revision to use for the build, may be null if
     *         this revision has not been initialised.
     */
    public synchronized Revision getRevision()
    {
        return revision;
    }

    /**
     * Check if this revision has been initialised.  The revision is initialised at the
     * latest when the build commences.
     *
     * @return true if this revision has been initialised.
     */
    public synchronized boolean isInitialised()
    {
        return getRevision() != null;
    }

    public synchronized boolean isUser()
    {
        return user;
    }

    public synchronized void setUser(boolean user)
    {
        this.user = user;
    }

    /**
     * Initialise the revision.
     *
     * @param revision  the revision to build
     */
    public synchronized void setRevision(Revision revision)
    {
        if (isInitialised())
        {
            throw new IllegalStateException("Attempt to update the revision");
        }
        this.revision = revision;
    }
}
