package com.zutubi.pulse.core;

/**
 * 
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(CommandContext context) throws BuildException;

    void prepare(String agent);
}
