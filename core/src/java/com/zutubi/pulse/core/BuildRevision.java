package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Revision;

/**
 * Represents the revision to be built.  This revision may change if the
 * build is queued for some time before commencing.  It may also be
 * determined lazily.
 */
public class BuildRevision
{
    /**
     * The revision to build, which may be null if it has not yet been
     * determined.
     */
    private Revision revision;
    /**
     * The pulse file corresponding to the revision.
     */
    private String pulseFile;
    /**
     * True if the revision has been determined and should *not* change from
     * now on.
     */
    private boolean fixed;
    /**
     * True if this is a revision explicitly specified by the user, when
     * triggering the build.
     */
    private boolean user;
    /**
     * The time at which the first recipe is dispatched: which is when the
     * build is said to have commenced.
     */
    private long timestamp = -1;

    /**
     * Construct a new revision that will be determined lazily, and thus is
     * not yet fixed.
     */
    public BuildRevision()
    {
        fixed = false;
        user = false;
    }

    /**
     * Create a new revision that will stay fixed at the given revision.
     *
     * @param revision the revision to build, which will not change.
     * @param pulseFile the pulse file corresponding to the revision
     */
    public BuildRevision(Revision revision, String pulseFile, boolean user)
    {
        assert(revision != null);

        this.revision = revision;
        this.pulseFile = pulseFile;
        fixed = true;
        this.user = user;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public String getPulseFile()
    {
        return pulseFile;
    }

    public boolean isInitialised()
    {
        return revision != null;
    }

    public boolean isFixed()
    {
        return fixed;
    }

    public boolean isUser()
    {
        return user;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * Update to a new revision, with the corresponding pulse file.  The
     * revision must not be fixed.
     *
     * @param revision  the new revision to build
     * @param pulseFile the pulse file corresponding to the revision
     */
    public void update(Revision revision, String pulseFile)
    {
        assert(!fixed);
        this.revision = revision;
        this.pulseFile = pulseFile;
    }

    /**
     * Applies this revision, updating the given recipe request.
     *
     * Called when a recipe for the build is dispatched.  At the first
     * dispatch the revision must be fixed, and the timestamp is set.
     *
     * @param request recipe request for the build, which will take on
     *                revision-specific information in this call
     */
    public void apply(RecipeRequest request)
    {
        request.setPulseFileSource(pulseFile);

        this.fixed = true;
        if(timestamp < 0)
        {
            timestamp = System.currentTimeMillis();
        }
    }
}
