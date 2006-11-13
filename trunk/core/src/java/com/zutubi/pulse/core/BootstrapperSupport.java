package com.zutubi.pulse.core;

/**
 * Helper base class for bootstrappers.
 */
public abstract class BootstrapperSupport implements Bootstrapper
{
    public void prepare(String agent)
    {
        // Do nothing by default
    }

    public void terminate()
    {
        // Nothing by default
    }
}
