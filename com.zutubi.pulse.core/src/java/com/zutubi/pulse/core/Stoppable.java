package com.zutubi.pulse.core;

/**
 * Generic interface for anything that can be stopped.
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
