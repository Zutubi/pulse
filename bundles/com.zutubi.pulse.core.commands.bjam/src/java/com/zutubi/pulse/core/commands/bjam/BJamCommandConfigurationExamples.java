package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

import java.io.File;

/**
 * Example configurations for the bjam command.
 */
public class BJamCommandConfigurationExamples
{
    public ConfigurationExample getBuildAndTest()
    {
        BJamCommandConfiguration command = new BJamCommandConfiguration();
        command.setName("build and test");
        command.setTargets("build test");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getCustomFileAndDir()
    {
        BJamCommandConfiguration command = new BJamCommandConfiguration();
        command.setName("test");
        command.setWorkingDir(new File("src"));
        command.setJamfile("MyJamfile");
        command.setTargets("test");
        return ExamplesBuilder.buildProject(command);
    }
}
