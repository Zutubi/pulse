package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;

/**
 * A post-processor for maven 3 output.  Attempts to capture features from Maven
 * itself (e.g. "[INFO] BUILD FAILURE") and from commonly-used plugins.
 */
public class Maven3PostProcessor extends PostProcessorGroup
{
    public Maven3PostProcessor(Maven3PostProcessorConfiguration config)
    {
        super(config.asGroup());
    }
}
