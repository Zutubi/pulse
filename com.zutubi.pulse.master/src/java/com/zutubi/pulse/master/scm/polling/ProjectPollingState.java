package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.core.scm.api.Revision;

/**
 * Records state from previous polls of a project.  This class is immutable,
 * each poll of a project returns a new state.
 */
public final class ProjectPollingState
{
    /**
     * Id of the project this state corresponds to.
     */
    private final long projectId;
    /**
     * The latest seen revision.  Note that for projects with a quiet period
     * this will not be changed until the quiet period is observed
     * successfully.  May be null if no revision has or could be obtained.
     */
    private final Revision latestRevision;
    /**
     * Time, in ms since the epoch, that the quiet period will end.  If the
     * project is not in a quiet period this will be zero.
     */
    private final long quietPeriodEnd;
    /**
     * Revision that started the latest quiet period.  Null when the project is
     * not within a quiet period.
     */
    private final Revision quietPeriodRevision;

    /**
     * Creates a new polling state recording a revision for the project.
     * 
     * @param projectId      id of the project this state belongs to
     * @param latestRevision the latest revision seen by a poll of the project
     */
    public ProjectPollingState(long projectId, Revision latestRevision)
    {
        this(projectId, latestRevision, 0, null);
    }

    /**
     * Creates a new polling state for a project within its quiet period.
     *
     * @param projectId           id of the project this state belongs to
     * @param latestRevision      the latest revision seen by a poll of the project before the quiet
     *                            period was started
     * @param quietPeriodEnd      time, in milliseconds since the epoch, that the quiet period ends
     * @param quietPeriodRevision latest revision to start (or restart) the quiet period
     */
    public ProjectPollingState(long projectId, Revision latestRevision, long quietPeriodEnd, Revision quietPeriodRevision)
    {
        this.projectId = projectId;
        this.latestRevision = latestRevision;
        this.quietPeriodEnd = quietPeriodEnd;
        this.quietPeriodRevision = quietPeriodRevision;
    }

    /**
     * @return the id of the project this state corresponds to
     */
    public long getProjectId()
    {
        return projectId;
    }

    /**
     * Indicates the latest revision seen by a poll, excluding revisions that
     * have started a new quiet period (those are held back while the quiet
     * period lapses).
     *
     * @return the latest revision at the time of the poll, may be null if this
     *         project has not been polled or the revision could not be
     *         obtained
     */
    public Revision getLatestRevision()
    {
        return latestRevision;
    }

    /**
     * @return true if this project was within a quiet period at the end of the
     *         last poll
     */
    public boolean isInQuietPeriod()
    {
        return quietPeriodEnd != 0;
    }

    /**
     * Indicates if a running quiet period has elapsed.  Only valid when {@link
     * #isInQuietPeriod()} returns true.
     *
     * @param now the current time in milliseconds since the epoch
     * @return true if the current quiet period has ended
     */
    public boolean hasQuietPeriodElapsed(long now)
    {
        return quietPeriodEnd < now;
    }

    /**
     * Indicates which revision was detected to start the current quiet period.
     * Only valid when {@link #isInQuietPeriod()} returns true.  If the quiet
     * period completes with no further revisions, this should become the
     * latest revision (and a change event should be raised).
     *
     * @return the new revision that started the current quiet period
     */
    public Revision getQuietPeriodRevision()
    {
        return quietPeriodRevision;
    }

    /**
     * Indicates if a new change has been detected in this state based on the
     * given previous state.
     *
     * @param previous the previous polling state to test against
     * @return true if a change has been detected since previous, and thus a
     *         change event should be raised
     */
    public boolean changeDetectedSince(ProjectPollingState previous)
    {
        return latestRevision != null && previous.latestRevision != null && !latestRevision.equals(previous.latestRevision);
    }

    @Override
    public String toString()
    {
        String s =  "pollstate(" + projectId + ": " + latestRevision;
        if (isInQuietPeriod())
        {
            s += ", quiet: " + quietPeriodRevision + " until " + quietPeriodEnd;
        }

        s += ")";
        return s;
    }
}
