package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the msbuild command.
 */
public class MsBuildCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        command.setName("build");
        command.setTargets("build");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getCustomised()
    {
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        command.setName("test");
        command.setBuildFile("myproject.proj");
        command.setTargets("build test");
        command.setConfiguration("Debug");
        command.addBuildProperty(new BuildPropertyConfiguration("foo", "bar"));
        return ExamplesBuilder.buildProject(command);
    }
}