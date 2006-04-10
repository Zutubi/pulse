package com.zutubi.pulse;

import com.zutubi.pulse.core.Stoppable;

import java.util.List;

/**
 * Manages orderly shutdown of the system.  The order is determined by a list
 * configured externally (e.g. via Spring).
 */
public class ShutdownManager
{
    private List<Stoppable> stoppables;

    /**
     * Performs the shutdown sequence.
     *
     * @param force if true, forcibly terminate active tasks
     */
    public void shutdown(boolean force)
    {
        for (Stoppable stoppable : stoppables)
        {
            stoppable.stop(force);
        }
    }

    public void setStoppables(List<Stoppable> stoppables)
    {
        this.stoppables = stoppables;
    }
}
