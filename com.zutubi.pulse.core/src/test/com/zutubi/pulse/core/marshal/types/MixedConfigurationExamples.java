package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Test examples-yielding companion class.
 */
public class MixedConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        MixedConfiguration example = new MixedConfiguration();
        example.setName("simple-example");
        example.getCompositeMap().put("foo", new TrivialConfiguration("foo"));
        return new ConfigurationExample("mixed", example);
    }
}
