package com.cinnamonbob.core;

/**
 */
public interface Stoppable
{
    /**
     * Requests that this Stoppable stops now.
     *
     * @param force if true, forceably terminate any oustanding tasks
     */
    void stop(boolean force);
}
