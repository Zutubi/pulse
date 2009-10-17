package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Command wrapping NAnt: just adds the default processor.
 */
public class NAntCommand extends NamedArgumentCommand
{
    public NAntCommand(NAntCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Arrays.<Class<? extends PostProcessorConfiguration>>asList(NAntPostProcessorConfiguration.class);
    }
}
