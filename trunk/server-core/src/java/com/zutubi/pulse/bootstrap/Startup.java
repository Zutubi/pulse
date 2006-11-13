package com.zutubi.pulse.bootstrap;

/**
 * Simplest possible startup interface.
 */
public interface Startup
{
    void init() throws StartupException;
    
    long getUptime();
}
