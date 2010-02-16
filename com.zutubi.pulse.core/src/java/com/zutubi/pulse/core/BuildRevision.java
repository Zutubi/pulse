package com.zutubi.pulse.core;

import com.zutubi.pulse.core.scm.api.Revision;

/**
 * Represents the revision to be built.  This revision may be fixed when the
 * build is triggered or determined lazily.  Once fixed, the build revision can
 * not be changed.
 */
public class BuildRevision
{
    /**
     * The revision to build, which may be null if it has not yet been
     * determined.
     */
    private Revision revision;
    /**
     * True if the revision has been determined.
     */
    private boolean fixed = false;
    /**
     * True if this is a revision explicitly specified by the user, when
     * triggering the build.
     */
    private boolean user = false;
    /**
     * The time at which the first recipe is dispatched: which is when the
     * build is said to have commenced.
     */
    private long timestamp = -1;

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
        fix();
    }

    /**
     * @return the underlying revision to use for the build, may be null if
     *         this revision has not been fixed.
     */
    public synchronized Revision getRevision()
    {
        return revision;
    }

    /**
     * Check if this revision has been fixed.  The revision is fixed at the
     * latest when the build commences.
     *
     * @return true if this revision has been fixed ({@link #setRevision} can no
     *         longer be called
     */
    public synchronized boolean isFixed()
    {
        return fixed;
    }

    /**
     * @return true if this revision was determined from user input
     */
    public boolean isUser()
    {
        return user;
    }

    /**
     * @return the time at which this revision was fixed, or -1 if it has not
     *         yet been fixed
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * Fix the revision.  The revision <strong>must not</strong> be fixed.
     *
     * @param revision  the revision to build
     */
    public void setRevision(Revision revision)
    {
        if (isFixed())
        {
            throw new IllegalStateException("Attempt to update a fixed revision");
        }

        this.revision = revision;
        fix();
    }

    /**
     * Fixes this revision, timestamping the moment when this occurs.  No
     * more updates are permitted after fixing.
     */
    private void fix()
    {
        timestamp = System.currentTimeMillis();
        this.fixed = true;
    }
}
