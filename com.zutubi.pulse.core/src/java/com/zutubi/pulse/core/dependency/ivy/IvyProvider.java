package com.zutubi.pulse.core.dependency.ivy;

/**
 * An interface that provides access to a configured ivy instance.
 */
public interface IvyProvider
{
    /**
     * Get a configured instance of support ivy.
     *
     * @return the configured ivy instance
     * @throws Exception is thrown if there is a problem configuring ivy.
     */
    IvySupport getIvySupport() throws Exception;
}
