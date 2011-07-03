package com.zutubi.pulse.core.engine.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.EnumUtils;

import static com.zutubi.util.CollectionUtils.indexOf;

/**
 * The possible states for a build, stage or command result.  Indicates what
 * step of the lifecycle the result has reached, and in the case of a completed
 * result whether it succeeded.
 */
public enum ResultState
{
    /**
     * The result has not yet commenced.
     */
    PENDING,
    
    /**
     * The result has commenced and is in progress (not yet completed).
     */
    IN_PROGRESS,

    /**
     * The result has been asked forcefully to complete, and will do so as soon
     * as possible.
     */
    TERMINATING,

    /**
     * The same as terminating, except that results in the cancelling state transition
     * to CANCELLED.
     *
     * @see #CANCELLED
     */
    CANCELLING,

    /**
     * The result indicates that the component was skipped.
     */
    SKIPPED,

    /**
     * The result completed successfully.
     */
    SUCCESS,

    /**
     * The result has completed and has failed due to a build problem.
     */
    FAILURE,

    /**
     * The result has completed and has failed due to an external problem.
     */
    ERROR,

    /**
     * Same as terminated, except that cancelled results have a low priority
     * when it comes to aggregating a builds result.  Where as a build with
     * failed and terminated results will be marked as terminated, a build with
     * failed and cancelled results will be marked as failed.
     */
    CANCELLED,

    /**
     * The result has completed but has failed because it was intentionally terminated.
     */
    TERMINATED;

    private static final ResultState[] BROKEN_STATES = new ResultState[]{ FAILURE, ERROR, TERMINATED, CANCELLED };
    private static final ResultState[] COMPLETED_STATES = new ResultState[]{SKIPPED, SUCCESS, FAILURE, ERROR, TERMINATED, CANCELLED };
    private static final ResultState[] INCOMPLETE_STATES = new ResultState[]{PENDING, IN_PROGRESS, TERMINATING, CANCELLING};
    private static final ResultState[] TERMINATED_STATES = new ResultState[]{CANCELLED, TERMINATED};
    private static final ResultState[] TERMINATING_STATES = new ResultState[]{CANCELLING, TERMINATING};

    private static final ResultState[] WORSE_STATE_ORDER = new ResultState[]{SKIPPED, SUCCESS, CANCELLED, FAILURE, ERROR, TERMINATED};

    /**
     * @return the set of states that return true from {@link #isBroken()}
     */
    public static ResultState[] getBrokenStates()
    {
        return BROKEN_STATES;
    }

    /**
     * @return the set of states that return true from {@link #isCompleted()}
     */
    public static ResultState[] getCompletedStates()
    {
        return COMPLETED_STATES;
    }

    /**
     * @return the set of states that return false from {@link #isCompleted()}
     */
    public static ResultState[] getIncompleteStates()
    {
        return INCOMPLETE_STATES;
    }

    /**
     * Returns the more severe of two completed states.  Errors take precedence
     * over failures, and failures over success.
     *
     * @param s1 the first completed state
     * @param s2 the second completed state
     * @return the more severe of the two completed states
     */
    public static ResultState getWorseState(ResultState s1, ResultState s2)
    {
        return getAggregate(s1, s2, WORSE_STATE_ORDER);
    }

    /**
     * Aggregate the two specified results according to the order specified.  That is, return
     * whichever of the two states appears later in the specified order.
     *
     * @param s1    a state to be aggregated
     * @param s2    a state to be aggregated
     * @param order the order of aggregation.  States of higher importance should appear
     * later in the list.
     *
     * @return the state that appears later in the specified order.
     */
    public static ResultState getAggregate(ResultState s1, ResultState s2, ResultState[] order)
    {
        return (indexOf(s1, order) < indexOf(s2, order)) ? s2 : s1;
    }

    /**
     * Parses a pretty string representation back into a state.
     *
     * @param prettyString the pretty string, as returned by {@link #getPrettyString()}
     * @return the state that has the given pretty string
     * @throws IllegalArgumentException if no state has the given pretty string
     */
    public static ResultState fromPrettyString(String prettyString)
    {
        return EnumUtils.fromPrettyString(ResultState.class, prettyString);
    }

    /**
     * @return true if this state indicates a result that is complete but
     *         unsuccessful
     */
    public boolean isBroken()
    {
        return CollectionUtils.contains(BROKEN_STATES, this);
    }

    /**
     * @return true if this state indicates a result that is complete
     */
    public boolean isCompleted()
    {
        return CollectionUtils.contains(COMPLETED_STATES, this);
    }

    /**
     * @return true if this state indicates this result is a terminated state.
     *
     * @see #TERMINATED
     * @see #CANCELLED
     */
    public boolean isTerminated()
    {
        return CollectionUtils.contains(TERMINATED_STATES, this);
    }

    /**
     * @return true if this state indicates this result is a terminating state.
     *
     * @see #TERMINATING
     * @see #CANCELLING
     */
    public boolean isTerminating()
    {
        return CollectionUtils.contains(TERMINATING_STATES, this);
    }

    /**
     * @return the final terminated state associated with the current result.
     *
     * @throws IllegalStateException if this state is not {@link #CANCELLING} or
     * {@link #TERMINATING}
     */
    public ResultState getTerminatedState()
    {
        switch (this)
        {
            case TERMINATING:
                return TERMINATED;
            case CANCELLING:
                return CANCELLED;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @return a human-readable string for this state
     */
    public String getPrettyString()
    {
        return EnumUtils.toPrettyString(this);
    }

    /**
     * @return an all lower-case machine-readable string for this state
     */
    public String getString()
    {
        return EnumUtils.toString(this);
    }
}
