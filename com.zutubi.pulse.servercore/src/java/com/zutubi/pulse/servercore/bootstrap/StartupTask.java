package com.zutubi.pulse.servercore.bootstrap;

/**
 */
public interface StartupTask
{
    /**
     * Execute the startup task
     *
     * @throws Exception on error.
     */
    void execute() throws Exception;

    /**
     * Indicates whether or not server startup should be halted if this
     * startup task fails.
     *
     * @return true if Pulse can not run with out this startup task
     * executing successfully.
     */
    boolean haltOnFailure();
}
