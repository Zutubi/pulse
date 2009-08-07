package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemArtifactConfigurationSupport;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of junit.pp.
 */
public class JUnitReportPostProcessorConfigurationExamples
{
    private static final String NAME = "junit.pp";

    public ConfigurationExample getSingleReport()
    {
        FileArtifactConfiguration output = new FileArtifactConfiguration();
        output.setName("junit xml report");
        output.setFile("build/reports/junit/TESTS-TestSuites.xml");
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
        JUnitReportPostProcessorConfiguration processor = new JUnitReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("ant", output);
    }
}