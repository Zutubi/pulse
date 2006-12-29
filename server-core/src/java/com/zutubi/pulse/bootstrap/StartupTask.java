package com.zutubi.pulse.bootstrap;

/**
 */
public interface StartupTask
{
    void execute();
    boolean haltOnFailure();
}
