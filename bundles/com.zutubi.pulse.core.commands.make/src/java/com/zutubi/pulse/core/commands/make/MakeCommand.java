package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 */
public class MakeCommand extends NamedArgumentCommand
{
    public MakeCommand(MakeCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Arrays.<Class<? extends PostProcessorConfiguration>>asList(MakePostProcessorConfiguration.class);
    }
}
