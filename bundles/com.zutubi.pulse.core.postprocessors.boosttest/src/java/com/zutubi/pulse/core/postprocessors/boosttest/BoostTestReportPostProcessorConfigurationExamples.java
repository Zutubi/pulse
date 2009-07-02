package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemOutputConfigurationSupport;
import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of boost-test.pp.
 */
public class BoostTestReportPostProcessorConfigurationExamples
{
    private static final String NAME = "boost-testpp.pp";

    public ConfigurationExample getSingleReport()
    {
        FileOutputConfiguration output = new FileOutputConfiguration();
        output.setName("boost.test xml report");
        output.setFile("build/reports/tests.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    public ConfigurationExample getReportDir()
    {
        DirectoryOutputConfiguration output = new DirectoryOutputConfiguration();
        output.setName("test reports");
        output.setBase("${base.dir}/reports");
        output.getInclusions().add("**/*.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    private ConfigurationExample buildProject(FileSystemOutputConfigurationSupport output)
    {
        BoostTestReportPostProcessorConfiguration processor = new BoostTestReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("make", output);
    }
}