package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.resources.api.SimpleBinaryResourceLocator;

/**
 * Locates the p4 binary.
 */
public class PerforceResourceLocator extends SimpleBinaryResourceLocator
{
    public PerforceResourceLocator()
    {
        super(PerforceConstants.RESOURCE_NAME, PerforceConstants.DEFAULT_P4);
    }
}
