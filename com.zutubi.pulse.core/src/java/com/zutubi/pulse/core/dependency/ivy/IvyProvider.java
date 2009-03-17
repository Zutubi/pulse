package com.zutubi.pulse.core.dependency.ivy;

/**
 * An interface that provides access to a configured ivy instance.
 */
public interface IvyProvider
{
    /**
     * Get a configured instance of support ivy.
     *
     * @param repositoryBase    defines the base path to the internal pulse repository.  The
     * format of this field must be a valid url.
     * @return  a new configured ivy support instance.
     *
     * @throws Exception on error.
     */
    IvySupport getIvySupport(String repositoryBase) throws Exception;
}
