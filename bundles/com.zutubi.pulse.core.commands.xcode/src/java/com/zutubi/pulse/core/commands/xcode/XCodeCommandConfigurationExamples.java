package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the xcode command.
 */
public class XCodeCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        XCodeCommandConfiguration command = new XCodeCommandConfiguration();
        command.setName("build");
        command.setTarget("test");
        return ExamplesBuilder.buildProject(command);
    }
}