package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    public Maven2PostProcessor(Maven2PostProcessorConfiguration config)
    {
        super(config.asGroup());
    }
}
