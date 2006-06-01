package com.zutubi.pulse.core;

/**
 * 
 */
public interface Bootstrapper
{
    /**
     * Called just before the recipe request containing this bootstrapper is
     * dispatched.
     */
    void prepare() throws PulseException;

    /**
     *
     */
    void bootstrap(RecipePaths paths) throws BuildException;

}
