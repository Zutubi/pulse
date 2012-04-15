package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;

public class MsBuildCommandTest extends ExecutableCommandTestCase
{
    private static final String BUILD_FILE_NAME = "build.proj";
    private static final String EXTENSION_XML = "xml";

    public boolean isMsBuildPresent()
    {
        return SystemUtils.IS_WINDOWS && SystemUtils.findInPath("msbuild") != null;
    }

    protected void runTest() throws Throwable
    {
        if (isMsBuildPresent())
        {
            super.runTest();
        }
    }

    public void testTrivialDefaultBuildFile() throws Exception
    {
        copyBuildFile("trivial");
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        successRun(command, "Build succeeded");
    }

    public void testExplicitBuildFile() throws Exception
    {
        final String NON_STANDARD_BUILD_FILE_NAME = "random.name";

        copyBuildFile("trivial");
        File buildFile = new File(baseDir, BUILD_FILE_NAME);
        assertTrue(buildFile.renameTo(new File(baseDir, NON_STANDARD_BUILD_FILE_NAME)));

        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        command.setBuildFile(NON_STANDARD_BUILD_FILE_NAME);
        successRun(command, "Build succeeded");
    }

    public void testNoBuildFile() throws Exception
    {
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        failedRun(command, "Specify a project or solution file");
    }

    public void testBadBuildFile() throws Exception
    {
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        command.setBuildFile("nosuchfile");
        failedRun(command, "Project file does not exist");
    }

    public void testCSharpSuccess() throws Exception
    {
        createSourceFile();

        copyBuildFile("csharp");
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        successRun(command, "Build succeeded");
    }

    public void testTargets() throws Exception
    {
        copyBuildFile("properties");
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        command.setTargets("T1 T2");
        successRun(command, "Ran T1", "Ran T2");
    }

    public void testConfiguration() throws Exception
    {
        copyBuildFile("properties");
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        command.setConfiguration("Release");
        successRun(command, "Configuration = Release");
    }

    public void testBuildProperties() throws Exception
    {
        copyBuildFile("properties");
        MsBuildCommandConfiguration command = new MsBuildCommandConfiguration();
        BuildPropertyConfiguration buildProperty = new BuildPropertyConfiguration();
        buildProperty.setName("foo");
        buildProperty.setValue("bar");
        command.getBuildProperties().put(buildProperty.getName(), buildProperty);
        successRun(command, "foo = bar");
    }

    private void createSourceFile() throws IOException
    {
        File sourceFile = new File(baseDir, "Test.cs");
        FileSystemUtils.createFile(sourceFile, "public class Test\n" +
                "{\n" +
                "    static int Main(string[] argv)\n" +
                "    {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}");
    }

    private TestCommandContext successRun(MsBuildCommandConfiguration commandConfiguration, String... contents) throws Exception
    {
        return successRun(new NamedArgumentCommand(commandConfiguration), contents);
    }

    private TestCommandContext failedRun(MsBuildCommandConfiguration commandConfiguration, String... contents) throws Exception
    {
        return failedRun(new NamedArgumentCommand(commandConfiguration), contents);
    }

    private File copyBuildFile(String name) throws IOException
    {
        return copyBuildFile(name, BUILD_FILE_NAME);
    }

    private File copyBuildFile(String name, String toName) throws IOException
    {
        return copyBuildFile(name, EXTENSION_XML, toName);
    }
}
