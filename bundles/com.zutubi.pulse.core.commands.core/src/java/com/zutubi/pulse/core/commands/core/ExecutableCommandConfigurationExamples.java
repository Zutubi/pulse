package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for executable.
 */
public class ExecutableCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        ExecutableCommandConfiguration command = new ExecutableCommandConfiguration("build");
        command.setExe("bash");
        command.setArgs("scripts/build.sh debug=true");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getEnvironment()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("compile.pp");
        pp.addErrorRegexes(": error");

        ExecutableCommandConfiguration command = new ExecutableCommandConfiguration("build");
        command.setExe("make");
        command.setArgs("-f MyMakefile test");
        command.addEnvironment(new EnvironmentConfiguration("BUILDMODE", "debug"));
        command.addPostProcessor(pp);

        return ExamplesBuilder.buildProject(command);
    }
}
