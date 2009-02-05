package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;

import java.util.Arrays;
import java.util.List;

/**
 */
public class AntCommand extends NamedArgumentCommand
{
    public AntCommand(AntCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<String> getDefaultPostProcessorNames()
    {
        return Arrays.asList(AntPostProcessorConfiguration.DEFAULT_PROCESSOR_NAME);
    }
}
