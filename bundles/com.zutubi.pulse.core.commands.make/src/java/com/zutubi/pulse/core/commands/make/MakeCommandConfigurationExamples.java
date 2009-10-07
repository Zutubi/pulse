package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the make command.
 */
public class MakeCommandConfigurationExamples
{
    public ConfigurationExample getBuildAndTest()
    {
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setName("build and test");
        command.setTargets("build test");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getCustomFileAndDir()
    {
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setName("test");
        command.setWorkingDir("src");
        command.setMakefile("MyMakefile");
        command.setTargets("test");
        return ExamplesBuilder.buildProject(command);
    }
}