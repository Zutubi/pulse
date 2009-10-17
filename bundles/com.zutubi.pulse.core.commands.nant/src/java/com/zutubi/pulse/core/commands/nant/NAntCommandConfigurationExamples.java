package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the nant command.
 */
public class NAntCommandConfigurationExamples
{
    public ConfigurationExample getSimpleBuild()
    {
        NAntCommandConfiguration command = new NAntCommandConfiguration();
        command.setName("build");
        command.setTargets("build");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getCustomBuildFile()
    {
        NAntCommandConfiguration command = new NAntCommandConfiguration();
        command.setBuildFile("my.build");
        command.setName("test");
        command.setTargets("build test");
        return ExamplesBuilder.buildProject(command);
    }
}
