package com.zutubi.pulse.master.cleanup;

/**
 * A cleanup request represents a potentially long running task.
 */
public interface CleanupRequest
{
    /**
     * Process the cleanup request.
     */
    void process();
}
