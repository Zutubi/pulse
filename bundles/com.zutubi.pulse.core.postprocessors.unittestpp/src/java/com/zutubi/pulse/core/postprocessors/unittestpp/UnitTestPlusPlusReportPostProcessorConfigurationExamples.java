package com.zutubi.pulse.core.postprocessors.unittestpp;

import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemOutputConfigurationSupport;
import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of unittestpp.pp.
 */
public class UnitTestPlusPlusReportPostProcessorConfigurationExamples
{
    private static final String NAME = "unittestpp.pp";

    public ConfigurationExample getSingleReport()
    {
        FileOutputConfiguration output = new FileOutputConfiguration();
        output.setName("unittest++ xml report");
        output.setFile("build/reports/UnitTestPP.xml");
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
        UnitTestPlusPlusReportPostProcessorConfiguration processor = new UnitTestPlusPlusReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("make", output);
    }
}