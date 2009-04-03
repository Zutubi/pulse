package com.zutubi.pulse.core.commands.ant;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example ant post-processors.
 */
public class AntPostProcessorConfigurationExamples
{
    private static final String NAME = "ant.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getContextNoFailOnError()
    {
        AntPostProcessorConfiguration pp = createEmpty();
        pp.setLeadingContext(10);
        pp.setFailOnError(false);
        return new ConfigurationExample(NAME, pp);
    }

    private AntPostProcessorConfiguration createEmpty()
    {
        AntPostProcessorConfiguration pp = new AntPostProcessorConfiguration(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}
