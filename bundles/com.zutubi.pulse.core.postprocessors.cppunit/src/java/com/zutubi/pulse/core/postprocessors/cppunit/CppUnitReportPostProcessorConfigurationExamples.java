package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemOutputConfigurationSupport;
import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of cppunit.pp.
 */
public class CppUnitReportPostProcessorConfigurationExamples
{
    private static final String NAME = "cppunit.pp";

    public ConfigurationExample getSingleReport()
    {
        FileOutputConfiguration output = new FileOutputConfiguration();
        output.setName("cppunit xml report");
        output.setFile("build/reports/CppUnit.xml");
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
        CppUnitReportPostProcessorConfiguration processor = new CppUnitReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("make", output);
    }
}
