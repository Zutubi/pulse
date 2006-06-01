package com.zutubi.pulse.core;

/**
 */
public interface Stoppable
{
    /**
     * Requests that this Stoppable stops now.
     *
     * @param force if true, forceably terminate any outstanding tasks
     */
    void stop(boolean force);
}
