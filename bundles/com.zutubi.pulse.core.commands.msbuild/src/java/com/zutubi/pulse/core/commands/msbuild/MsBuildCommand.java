package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 */
public class MsBuildCommand extends NamedArgumentCommand
{
    public MsBuildCommand(MsBuildCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Arrays.<Class<? extends PostProcessorConfiguration>>asList(MsBuildPostProcessorConfiguration.class);
    }
}
