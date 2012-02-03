package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.resources.api.SimpleBinaryResourceLocator;

/**
 * Locates the hg binary.
 */
public class MercurialResourceLocator extends SimpleBinaryResourceLocator
{
    public MercurialResourceLocator()
    {
        super(MercurialConstants.RESOURCE_NAME, MercurialConstants.DEFAULT_HG);
    }
}
