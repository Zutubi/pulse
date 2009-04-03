package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for sleep.
 */
public class SleepCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        SleepCommandConfiguration command = new SleepCommandConfiguration("pause");
        command.setInterval(10000);
        return ExamplesBuilder.buildProject(command);
    }
}
