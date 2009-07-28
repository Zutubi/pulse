package com.zutubi.pulse.core;

import com.zutubi.pulse.core.scm.api.Revision;

import java.util.concurrent.locks.ReentrantLock;

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
     * True if the revision has been determined and should *not* change from
     * now on.
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
     * Must be held to update or fix this revision, or to take decisions
     * based on whether the revision is fixed.
     */
    private ReentrantLock lock = new ReentrantLock();

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
     *         this revision has not been initialised
     */
    public Revision getRevision()
    {
        return revision;
    }

    /**
     * Check if this revision has been initialised.  This revision must be
     * locked to make this call, and the lock should be held while relying on
     * the result of this call.
     *
     * @return true if this revision has been intialised (its underlying
     *         revision and pulse file have been set)
     */
    public boolean isInitialised()
    {
        checkLocked();
        return revision != null;
    }

    /**
     * Check if this revision has been fixed.  The revision is fixed at the
     * latest when the build commences.  This revision must be locked to make
     * this call, and the lock should be held while relying on the result of
     * this call.
     *
     * @return true if this revision has been fixed ({@link #update} can no
     *         longer be called
     */
    public boolean isFixed()
    {
        checkLocked();
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
     * Take a lock on this revision, required for various other calls.  The
     * lock is reentrant, i.e. the same thread may lock multiple times.
     */
    public void lock()
    {
        lock.lock();
    }

    /**
     * Release a lock on this revision, the calling thread must hold the lock.
     *
     * @throws IllegalStateException if the calling thread does not hold the
     *                              lock
     */
    public void unlock()
    {
        lock.unlock();
    }

    /**
     * @return true iff this revision is currently locked by the calling
     *         thread
     */
    public boolean isLocked()
    {
        return lock.isHeldByCurrentThread();
    }

    private void checkLocked()
    {
        if (!isLocked())
        {
            throw new IllegalStateException("Call to method requiring lock without holding lock");
        }
    }

    /**
     * Update to a new revision.  The revision <strong>must not</strong> be
     * fixed, and it <strong>must</strong> be locked when making this call.
     *
     * @param revision  the new revision to build
     */
    public void update(Revision revision)
    {
        checkLocked();

        if (fixed)
        {
            throw new IllegalStateException("Attempt to update a fixed revision");
        }

        this.revision = revision;
    }

    /**
     * Fixes this revision, timestamping the moment when this occurs.  No
     * more updates are permitted after fixing.
     */
    public void fix()
    {
        checkLocked();
        timestamp = System.currentTimeMillis();
        this.fixed = true;
    }
}
