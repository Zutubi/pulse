package com.zutubi.pulse.core.engine.api;

import static com.zutubi.util.CollectionUtils.indexOf;
import com.zutubi.util.EnumUtils;

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
    PENDING(false, false),
    
    /**
     * The result has commenced and is in progress (not yet completed).
     */
    IN_PROGRESS(false, false),

    /**
     * The result has been asked forcefully to complete, and will do so as soon
     * as possible.
     */
    TERMINATING(false, false),

    /**
     * The result indicates that the component was skipped.
     */
    SKIPPED(true, false),

    /**
     * The result completed successfully.
     */
    SUCCESS(true, false),

    /**
     * The result has completed and has failed due to a build problem.
     */
    FAILURE(true, true),

    /**
     * The result has completed and has failed due to an external problem.
     */
    ERROR(true, true),

    /**
     * The result has completed but has failed because it was intentionally terminated.
     */
    TERMINATED(true, true);

    private static final ResultState[] BROKEN_STATES;
    private static final ResultState[] COMPLETED_STATES;
    private static final ResultState[] INCOMPLETE_STATES;
    
    private static final ResultState[] WORSE_STATE_ORDER = new ResultState[]{SKIPPED, SUCCESS, FAILURE, ERROR, TERMINATED};

    static
    {
        int broken = 0;
        int complete = 0;
        int incomplete = 0;
        for(ResultState state: values())
        {
            if(state.isBroken())
            {
                broken++;
            }

            if(state.isCompleted())
            {
                complete++;
            }
            else
            {
                incomplete++;
            }
        }

        BROKEN_STATES = new ResultState[broken];
        COMPLETED_STATES = new ResultState[complete];
        INCOMPLETE_STATES = new ResultState[incomplete];

        broken = 0;
        complete = 0;
        incomplete = 0;
        for(ResultState state: values())
        {
            if(state.isBroken())
            {
                BROKEN_STATES[broken++] = state;
            }

            if(state.isCompleted())
            {
                COMPLETED_STATES[complete++] = state;
            }
            else
            {
                INCOMPLETE_STATES[incomplete++] = state;
            }
        }
    }

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
        assertCompleted(s1);
        assertCompleted(s2);

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
     * @return the state that appears earlier in the specified order.
     */
    public static ResultState getAggregate(ResultState s1, ResultState s2, ResultState[] order)
    {
        return (indexOf(s1, order) < indexOf(s2, order)) ? s2 : s1;
    }

    private static void assertCompleted(ResultState state)
    {
        if (state != null && !state.isCompleted())
        {
            throw new IllegalArgumentException("Completed result state expected.  Instead found " + state);
        }
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
     * True if this state indicates a result that is unsuccessful.
     */
    private boolean broken;

    /**
     * True if this state indicates a result that is complete.
     */
    private boolean completed;

    private ResultState(boolean completed, boolean broken)
    {
        this.broken = broken;
        this.completed = completed;
    }

    /**
     * @return true if this state indicates a result that is complete but
     *         unsuccessful
     */
    public boolean isBroken()
    {
        return broken;
    }

    /**
     * @return true if this state indicates a result that is complete
     */
    public boolean isCompleted()
    {
        return completed;
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
