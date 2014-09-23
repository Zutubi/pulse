package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

import java.util.Arrays;

/**
 * Example configurations for the xcode command.
 */
public class XCodeCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        XCodeCommandConfiguration command = new XCodeCommandConfiguration();
        command.setName("build");
        command.setTarget("MyApp");
        command.setBuildaction("clean build");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getWorkspace()
    {
        XCodeCommandConfiguration command = new XCodeCommandConfiguration();
        command.setName("ios tests");
        command.setWorkspace("MyWorkspace.xcworkspace");
        command.setScheme("MyProjectTests");
        command.setDestinations(Arrays.asList("platform=iOS Simulator,name=iPhone 5s", "platform=iOS,name=My iPad"));
        command.setBuildaction("test");
        return ExamplesBuilder.buildProject(command);
    }
}