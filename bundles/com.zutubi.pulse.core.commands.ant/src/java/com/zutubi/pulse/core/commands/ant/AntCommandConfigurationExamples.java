package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the ant command.
 */
public class AntCommandConfigurationExamples
{
    public ConfigurationExample getSimpleBuild()
    {
        AntCommandConfiguration command = new AntCommandConfiguration();
        command.setName("build");
        command.setTargets("build");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getCustomBuildFile()
    {
        AntCommandConfiguration command = new AntCommandConfiguration();
        command.setBuildFile("mybuild.xml");
        command.setName("test");
        command.setTargets("build test");
        return ExamplesBuilder.buildProject(command);
    }
}
