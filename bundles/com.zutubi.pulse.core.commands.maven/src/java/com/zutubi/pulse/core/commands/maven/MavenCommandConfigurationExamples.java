package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the maven command.
 */
public class MavenCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setName("build");
        command.setTargets("test");
        return ExamplesBuilder.buildProject(command);
    }
}