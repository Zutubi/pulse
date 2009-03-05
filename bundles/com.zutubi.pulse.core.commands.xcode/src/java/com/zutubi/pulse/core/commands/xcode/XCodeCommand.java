package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 */
public class XCodeCommand extends NamedArgumentCommand
{
    public XCodeCommand(XCodeCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Arrays.<Class<? extends PostProcessorConfiguration>>asList(XCodePostProcessorConfiguration.class);
    }
}
