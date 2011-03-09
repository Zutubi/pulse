package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the maven3 command.
 */
public class Maven3CommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        Maven3CommandConfiguration command = new Maven3CommandConfiguration();
        command.setName("build");
        command.setGoals("test");
        return ExamplesBuilder.buildProject(command);
    }
}