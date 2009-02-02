package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;

/**
 */
public class MavenPostProcessor extends PostProcessorGroup
{
    public MavenPostProcessor(MavenPostProcessorConfiguration config)
    {
        super(config.asGroup());
    }
}
