package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemOutputConfigurationSupport;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of junit.pp.
 */
public class JUnitReportPostProcessorConfigurationExamples
{
    private static final String NAME = "junit.pp";

    public ConfigurationExample getSingleReport()
    {
        FileOutputConfiguration output = new FileOutputConfiguration();
        output.setName("junit xml report");
        output.setFile("build/reports/junit/TESTS-TestSuites.xml");
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
        JUnitReportPostProcessorConfiguration processor = new JUnitReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("ant", output);
    }
}