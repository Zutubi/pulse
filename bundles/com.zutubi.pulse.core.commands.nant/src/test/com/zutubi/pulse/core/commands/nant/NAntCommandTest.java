package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandTestCase;
import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;

public class NAntCommandTest extends OutputProducingCommandTestCase
{
    private static final String EXTENSION_XML = "xml";

    public void testBasicDefault() throws Exception
    {
        if (SystemUtils.IS_WINDOWS)
        {
            File destinationFile = new File(baseDir, "default.build");
            IOUtils.joinStreams(getInput(getName(), EXTENSION_XML), new FileOutputStream(destinationFile), true);

            TestCommandContext context = runCommand(new NamedArgumentCommand(new NAntCommandConfiguration()));
            assertEquals(ResultState.SUCCESS, context.getResultState());
            assertArtifactRegistered(new TestCommandContext.Artifact(OutputProducingCommandSupport.OUTPUT_NAME), context);
        }
    }

    public void testNoBuildFile() throws Exception
    {
        if (SystemUtils.IS_WINDOWS)
        {
            TestCommandContext context = runCommand(new NamedArgumentCommand(new NAntCommandConfiguration()));
            assertEquals(ResultState.FAILURE, context.getResultState());
            assertDefaultOutputContains("Could not find a '*.build' file");
        }
    }

    public void testSetBuildFile() throws Exception
    {
        if (SystemUtils.IS_WINDOWS)
        {
            copyInputToDirectory(EXTENSION_XML, baseDir);

            NAntCommandConfiguration commandConfiguration = new NAntCommandConfiguration();
            commandConfiguration.setBuildFile(getBuildFilename());
            TestCommandContext context = runCommand(new NamedArgumentCommand(commandConfiguration));
            assertEquals(ResultState.SUCCESS, context.getResultState());
        }
    }

    public void testSetBuildFileNonExistant() throws Exception
    {
        if (SystemUtils.IS_WINDOWS)
        {
            NAntCommandConfiguration commandConfiguration = new NAntCommandConfiguration();
            commandConfiguration.setBuildFile("custom.build");
            TestCommandContext context = runCommand(new NamedArgumentCommand(commandConfiguration));
            assertEquals(ResultState.FAILURE, context.getResultState());
            assertDefaultOutputContains("Could not find file", "custom.build");
        }
    }

    public void testSetTargets() throws Exception
    {
        if (SystemUtils.IS_WINDOWS)
        {
            copyInputToDirectory(EXTENSION_XML, baseDir);

            NAntCommandConfiguration commandConfiguration = new NAntCommandConfiguration();
            commandConfiguration.setBuildFile(getBuildFilename());
            commandConfiguration.setTargets("run1 run2");
            TestCommandContext context = runCommand(new NamedArgumentCommand(commandConfiguration));
            assertEquals(ResultState.SUCCESS, context.getResultState());
            assertDefaultOutputContains("run1", "run2");
        }
    }

    private String getBuildFilename()
    {
        return getName() + "." + EXTENSION_XML;
    }
}
