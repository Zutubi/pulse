package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.core.ExecutableCommandConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * A project configuration setup for working with the wait ant projects.
 */
public class WaitProject extends ProjectConfigurationHelper
{
    private static final String JAR = "awaitfile.jar";
    private static final String WAIT_TIMEOUT = "300";

    private File waitFile;

    public WaitProject(ProjectConfiguration config, File tmpDir)
    {
        super(config);
        waitFile =  new File(tmpDir, getConfig().getName());
        if (waitFile.exists() && !waitFile.delete())
        {
            throw new RuntimeException("Unable to clean up wait file '" + waitFile.getAbsolutePath() + "'");
        }
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        ExecutableCommandConfiguration command = new ExecutableCommandConfiguration();
        command.setName(ProjectConfigurationWizard.DEFAULT_COMMAND);
        command.setExe("java");
        command.setExtraArguments(asList("-jar", JAR, waitFile.getAbsolutePath().replace("\\", "/"), WAIT_TIMEOUT));
        return command;
    }

    public List<String> getPostProcessorNames()
    {
        return asList("junit xml report processor");
    }

    public void releaseBuild() throws IOException
    {
        FileSystemUtils.createFile(waitFile, "test");
    }

    public List<Class> getPostProcessorTypes()
    {
        return Arrays.<Class>asList(JUnitReportPostProcessorConfiguration.class);
    }
}
