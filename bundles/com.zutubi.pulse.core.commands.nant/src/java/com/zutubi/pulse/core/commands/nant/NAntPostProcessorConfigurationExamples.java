package com.zutubi.pulse.core.commands.nant;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example nant post-processors.
 */
public class NAntPostProcessorConfigurationExamples
{
    private static final String NAME = "nant.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getContextNoFailOnError()
    {
        NAntPostProcessorConfiguration pp = createEmpty();
        pp.setLeadingContext(10);
        pp.setFailOnError(false);
        return new ConfigurationExample(NAME, pp);
    }

    private NAntPostProcessorConfiguration createEmpty()
    {
        NAntPostProcessorConfiguration pp = new NAntPostProcessorConfiguration(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}
