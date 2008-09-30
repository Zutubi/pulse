package com.zutubi.pulse.core;

/**
 * Bootstrappers are used to initialise a working area ready for a build.
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(ExecutionContext context) throws BuildException;
    void terminate();
}
