package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.Ivy;

/**
 * An interface that provides access to a configured ivy instance.
 */
public interface IvyProvider
{
    /**
     * Get a configured instance of ivy.
     *
     * @return the configured ivy instance
     * @throws Exception is thrown if there is a problem configuring ivy.
     */
    Ivy getIvy() throws Exception;
}
