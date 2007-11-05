package com.zutubi.pulse.core;

/**
 * 
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(ExecutionContext context) throws BuildException;

    void prepare(String agent);

    void terminate();
}
