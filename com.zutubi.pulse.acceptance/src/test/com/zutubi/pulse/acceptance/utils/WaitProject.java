/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.utils;

import com.google.common.io.Files;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.core.ExecutableCommandConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

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

    public WaitProject(ProjectConfiguration config, ConfigurationHelper helper, File tmpDir, boolean cleanup)
    {
        super(config, helper);
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
        Files.write("test", waitFile, Charset.defaultCharset());
    }

    private File getWaitFile(String stageName)
    {
        return new File(waitBaseDir, stageName);
    }
}
