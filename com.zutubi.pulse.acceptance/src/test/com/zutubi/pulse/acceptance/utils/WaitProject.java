package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.core.ExecutableCommandConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.util.FileSystemUtils;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A project configuration setup for working with the wait ant projects.
 *
 * This project will block until released. See {@link #releaseBuild()} and
 * {@link #releaseStage(String)}. 
 */
public class WaitProject extends ProjectConfigurationHelper
{
    private static final String WAIT_FILE_PROPERTY = "waitfile";
    private static final String JAR = "awaitfile.jar";
    private static final String WAIT_TIMEOUT = "300";

    private File waitBaseDir;
    private boolean cleanup;

    public WaitProject(ProjectConfiguration config, File tmpDir, boolean cleanup)
    {
        super(config);
        this.cleanup = cleanup;
        waitBaseDir = new File(tmpDir, config.getName());

        File[] files = waitBaseDir.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                try
                {
                    FileSystemUtils.delete(f);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<String> getPostProcessorNames()
    {
        return asList("junit xml report processor");
    }

    public List<Class> getPostProcessorTypes()
    {
        return Arrays.<Class>asList(JUnitReportPostProcessorConfiguration.class);
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        ExecutableCommandConfiguration command = new ExecutableCommandConfiguration();
        command.setName(ProjectConfigurationWizard.DEFAULT_COMMAND);
        command.setExe("java");
        List<String> arguments = new LinkedList<String>(asList("-jar", JAR, "$(" + WAIT_FILE_PROPERTY + ")", WAIT_TIMEOUT));
        if (cleanup)
        {
            arguments.add("true");
        }
        
        command.setExtraArguments(arguments);
        return command;
    }

    @Override
    public BuildStageConfiguration addStage(String stageName)
    {
        BuildStageConfiguration stage = super.addStage(stageName);
        addStageProperty(stage, WAIT_FILE_PROPERTY, getWaitFile(stageName).getAbsolutePath().replace("\\", "/"));
        return stage;
    }

    public void releaseBuild() throws IOException
    {
        for (String stageName : getConfig().getStages().keySet())
        {
            releaseStage(stageName);
        }
    }

    public void releaseStage(String stageName) throws IOException
    {
        File waitFile = getWaitFile(stageName);
        File dir = waitFile.getParentFile();
        if (!dir.isDirectory() && !dir.mkdirs())
        {
            throw new IOException("Failed to create directory " + dir.getAbsolutePath());
        }
        FileSystemUtils.createFile(waitFile, "test");
    }

    private File getWaitFile(String stageName)
    {
        return new File(waitBaseDir, stageName);
    }
}
