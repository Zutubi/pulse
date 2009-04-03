package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemOutputConfigurationSupport;
import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.pulse.core.commands.core.ExecutableCommandConfiguration;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations of cunit.pp.
 */
public class CUnitReportPostProcessorConfigurationExamples
{
    private static final String NAME = "cunit.pp";

    public ConfigurationExample getSingleReport()
    {
        FileOutputConfiguration output = new FileOutputConfiguration();
        output.setName("cunit xml report");
        output.setFile("build/reports/CUnit.xml");
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
        CUnitReportPostProcessorConfiguration processor = new CUnitReportPostProcessorConfiguration();
        processor.setName(NAME);

        output.addPostProcessor(processor);

        ExecutableCommandConfiguration exe = new ExecutableCommandConfiguration();
        exe.setName("build");
        exe.setExe("make");
        exe.addOutput(output);

        return ExamplesBuilder.buildProjectForCaptureProcessor(exe, processor);
    }
}