package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemOutputConfigurationSupport;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of junitee.pp.
 */
public class JUnitEEReportPostProcessorConfigurationExamples
{
    private static final String NAME = "junitee.pp";

    public ConfigurationExample getSingleReport()
    {
        FileOutputConfiguration output = new FileOutputConfiguration();
        output.setName("junitee xml report");
        output.setFile("build/reports/junitee/TESTS-TestSuites.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    public ConfigurationExample getReportDir()
    {
        DirectoryOutputConfiguration output = new DirectoryOutputConfiguration();
        output.setName("test reports");
        output.setBase("${base.dir}/reports");
        output.getInclusions().add("**/TEST-*.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    private ConfigurationExample buildProject(FileSystemOutputConfigurationSupport output)
    {
        JUnitEEReportPostProcessorConfiguration processor = new JUnitEEReportPostProcessorConfiguration();
        processor.setName(NAME);

        output.addPostProcessor(processor);

        ExecutableCommandConfiguration exe = new ExecutableCommandConfiguration();
        exe.setName("build");
        exe.setExe("ant");
        exe.addOutput(output);

        return ExamplesBuilder.buildProjectForCaptureProcessor(exe, processor);
    }
}