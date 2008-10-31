package com.zutubi.pulse.servercore.bootstrap;

/**
 */
public interface StartupTask
{
    void execute();
    boolean haltOnFailure();
}
