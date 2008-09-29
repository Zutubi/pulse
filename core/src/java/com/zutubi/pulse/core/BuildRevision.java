package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Revision;

import java.util.concurrent.locks.Lock;
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
     * The pulse file corresponding to the revision.
     */
    private String pulseFile;
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
    private Lock lock = new ReentrantLock();
    /**
     * Positive when {@link #lock} is locked, used to enforce locking on
     * callers.
     */
    private int lockEntries = 0;

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
     * @param pulseFile the pulse file corresponding to the revision
     * @param user      if true, this is a user-specified revision (as
     *                  opposed to a fixed revision decided on by Pulse
     *                  itself, as happens e.g. when isolating changes).
     */
    public BuildRevision(Revision revision, String pulseFile, boolean user)
    {
        if (revision == null)
        {
            throw new NullPointerException("Revision may not be null");
        }

        if (pulseFile == null)
        {
            throw new NullPointerException("Pulse file may not be null");
        }

        this.revision = revision;
        this.pulseFile = pulseFile;
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
     * @return the pulse file for the project at our current revision, may be
     *         null if this revision has not been initialised
     */
    public String getPulseFile()
    {
        return pulseFile;
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
        lockEntries++;
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
        lockEntries--;
    }

    /**
     * @return true iff this revision is currently locked
     */
    public boolean isLocked()
    {
        return lockEntries > 0;
    }

    private void checkLocked()
    {
        if (!isLocked())
        {
            throw new IllegalStateException("Call to method requiring lock without holding lock");
        }
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
        checkLocked();

        if (fixed)
        {
            throw new IllegalStateException("Attempt to update a fixed revision");
        }

        this.revision = revision;
        this.pulseFile = pulseFile;
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
