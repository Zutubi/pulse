package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the maven2 command.
 */
public class Maven2CommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        Maven2CommandConfiguration command = new Maven2CommandConfiguration();
        command.setName("build");
        command.setGoals("test");
        return ExamplesBuilder.buildProject(command);
    }
}