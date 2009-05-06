package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 * Resolves to the browse section.
 */
public class BrowseActionResolver extends StaticMapActionResolver
{
    public BrowseActionResolver()
    {
        super("browse");
        addMapping("projects", new BrowseProjectsActionResolver());
    }
}
