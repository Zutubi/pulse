package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.resources.api.SimpleBinaryResourceLocator;

/**
 * Locates a make resource by finding the make binary on the path.
 */
public class MakeResourceLocator extends SimpleBinaryResourceLocator
{
    public MakeResourceLocator()
    {
        super("make");
    }
}
