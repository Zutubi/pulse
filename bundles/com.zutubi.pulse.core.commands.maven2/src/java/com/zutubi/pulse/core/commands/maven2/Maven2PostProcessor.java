package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;

/**
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    public Maven2PostProcessor(Maven2PostProcessorConfiguration config)
    {
        super(config.asGroup());
    }
}
