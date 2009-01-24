package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.core.ExecutableCommandConfiguration;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;

import java.io.File;
import java.io.IOException;

public class AntCommandTest extends ExecutableCommandTestCase
{
    private static final String EXTENSION_XML = "xml";

    public void testBasicDefault() throws Exception
    {
        copyBuildFile("basic");

        AntCommand command = new AntCommand(new AntCommandConfiguration());
        successRun(command, "build target");
    }

    public void testBasicTargets() throws Exception
    {
        copyBuildFile("basic");

        AntCommandConfiguration config = new AntCommandConfiguration();
        config.setTargets("build test");

        AntCommand command = new AntCommand(config);
        successRun(command, "build target", "test target");
    }

    public void testDoubleSpaceTargets() throws Exception
    {
        copyBuildFile("basic");

        AntCommandConfiguration config = new AntCommandConfiguration();
        config.setTargets("build  test");

        AntCommand command = new AntCommand(config);
        successRun(command, "build target", "test target");
    }

    public void testEnvironment() throws Exception
    {
        copyBuildFile("basic");

        AntCommandConfiguration config = new AntCommandConfiguration();
        config.setTargets("environment");
        config.getEnvironments().add(new ExecutableCommandConfiguration.EnvironmentConfiguration("TEST_ENV_VAR", "test variable value"));

        AntCommand command = new AntCommand(config);
        successRun(command, "test variable value");
    }

    public void testExplicitBuildfile() throws Exception
    {
        copyBuildFile("basic",  "custom.xml");

        AntCommandConfiguration config = new AntCommandConfiguration();
        config.setBuildFile("custom.xml");

        AntCommand command = new AntCommand(config);
        successRun(command, "build target");
    }

    public void testExplicitArguments() throws Exception
    {
        copyBuildFile("basic",  "custom.xml");

        AntCommandConfiguration config = new AntCommandConfiguration();
        config.setBuildFile("custom.xml");
        config.setTargets("build");
        config.setArgs("test");

        AntCommand command = new AntCommand(config);
        successRun(command, "build target", "test target");
    }

    public void testRunNoBuildFile() throws Exception
    {
        AntCommand command = new AntCommand(new AntCommandConfiguration());
        failedRun(command, "Buildfile: build.xml does not exist!");
    }

    public void testRunNonExistantBuildFile() throws Exception
    {
        AntCommandConfiguration config = new AntCommandConfiguration();
        config.setBuildFile("nope.xml");

        AntCommand command = new AntCommand(config);
        failedRun(command, "Buildfile: nope.xml does not exist!");
    }

    private File copyBuildFile(String name) throws IOException
    {
        return copyBuildFile(name, "build.xml");
    }

    private File copyBuildFile(String name, String toName) throws IOException
    {
        File buildFile = copyInputToDirectory(name, EXTENSION_XML, baseDir);
        assertTrue(buildFile.renameTo(new File(baseDir, toName)));
        return buildFile;
    }

    protected String getBuildFileName()
    {
        return "build.xml";
    }

    protected String getBuildFileExt()
    {
        return EXTENSION_XML;
    }
}
