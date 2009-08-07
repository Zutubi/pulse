package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemArtifactConfigurationSupport;
import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of cunit.pp.
 */
public class CUnitReportPostProcessorConfigurationExamples
{
    private static final String NAME = "cunit.pp";

    public ConfigurationExample getSingleReport()
    {
        FileArtifactConfiguration output = new FileArtifactConfiguration();
        output.setName("cunit xml report");
        output.setFile("build/reports/CUnit.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    public ConfigurationExample getReportDir()
    {
        DirectoryArtifactConfiguration output = new DirectoryArtifactConfiguration();
        output.setName("test reports");
        output.setBase("${base.dir}/reports");
        output.getInclusions().add("**/*.xml");
        output.setFailIfNotPresent(false);

        return buildProject(output);
    }

    private ConfigurationExample buildProject(FileSystemArtifactConfigurationSupport output)
    {
        CUnitReportPostProcessorConfiguration processor = new CUnitReportPostProcessorConfiguration();
        processor.setName(NAME);
        output.addPostProcessor(processor);

        return ExamplesBuilder.buildProjectForCaptureProcessor("make", output);
    }
}