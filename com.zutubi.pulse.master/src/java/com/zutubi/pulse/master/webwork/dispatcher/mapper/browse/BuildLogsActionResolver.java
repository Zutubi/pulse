package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 * Resolves to the build logs tab.  The URL may optionally further specify to
 * show the build log or a specific stage log.
 */
public class BuildLogsActionResolver extends StaticMapActionResolver
{
    public BuildLogsActionResolver()
    {
        super("tailRecipeLog");

        addMapping("build", new BuildLogActionResolver());
        addMapping("stage", new StageLogsActionResolver());        
    }
}
