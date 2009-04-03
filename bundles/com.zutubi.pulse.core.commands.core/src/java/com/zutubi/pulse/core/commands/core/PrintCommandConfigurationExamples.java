package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for print.
 */
public class PrintCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        PrintCommandConfiguration command = new PrintCommandConfiguration("welcome");
        command.setMessage("Hello, world!");
        return ExamplesBuilder.buildProject(command);
    }
}