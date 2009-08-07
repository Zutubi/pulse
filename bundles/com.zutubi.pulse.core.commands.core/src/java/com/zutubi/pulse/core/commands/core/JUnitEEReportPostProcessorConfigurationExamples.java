package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemArtifactConfigurationSupport;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of junitee.pp.
 */
public class JUnitEEReportPostProcessorConfigurationExamples
{
    private static final String NAME = "junitee.pp";

    public ConfigurationExample getSingleReport()
    {
        FileArtifactConfiguration output = new FileArtifactConfiguration();
        output.setName("junitee xml report");
        output.setFile("build/reports/junitee/TESTS-TestSuites.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    public ConfigurationExample getReportDir()
    {
        DirectoryArtifactConfiguration output = new DirectoryArtifactConfiguration();
        output.setName("test reports");
        output.setBase("${base.dir}/reports");
        output.getInclusions().add("**/TEST-*.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    private ConfigurationExample buildProject(FileSystemArtifactConfigurationSupport output)
    {
        JUnitEEReportPostProcessorConfiguration processor = new JUnitEEReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("ant", output);
    }
}