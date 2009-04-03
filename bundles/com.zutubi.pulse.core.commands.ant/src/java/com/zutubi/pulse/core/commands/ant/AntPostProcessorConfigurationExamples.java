package com.zutubi.pulse.core.commands.ant;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example ant post-processors.
 */
public class AntPostProcessorConfigurationExamples
{
    private static final String ELEMENT = "ant.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(ELEMENT, createEmpty());
    }

    public ConfigurationExample getContextNoFailOnError()
    {
        AntPostProcessorConfiguration pp = createEmpty();
        pp.setLeadingContext(10);
        pp.setFailOnError(false);
        return new ConfigurationExample(ELEMENT, pp);
    }

    private AntPostProcessorConfiguration createEmpty()
    {
        AntPostProcessorConfiguration pp = new AntPostProcessorConfiguration("ant.pp");
        pp.getPatterns().clear();
        return pp;
    }
}
