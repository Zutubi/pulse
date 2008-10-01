package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.ExecutableCommandTestBase;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;

public class MsBuildCommandTest extends ExecutableCommandTestBase
{
    public boolean isMsBuildPresent()
    {
        return SystemUtils.IS_WINDOWS && SystemUtils.findInPath("msbuild") != null;
    }

    public void testTrivialDefaultBuildFile() throws Exception
    {
        if (isMsBuildPresent())
        {
            copyBuildFileToBaseDir("trivial");
            MsBuildCommand command = new MsBuildCommand();
            successRun(command, "Build succeeded");
        }
    }

    public void testExplicitBuildFile() throws Exception
    {
        if (isMsBuildPresent())
        {
            final String NON_STANDARD_BUILD_FILE_NAME = "random.name";

            copyBuildFileToBaseDir("trivial");
            File buildFile = new File(baseDir, getBuildFileName());
            assertTrue(buildFile.renameTo(new File(baseDir, NON_STANDARD_BUILD_FILE_NAME)));

            MsBuildCommand command = new MsBuildCommand();
            command.setBuildFile(NON_STANDARD_BUILD_FILE_NAME);
            successRun(command, "Build succeeded");
        }
    }

    public void testNoBuildFile() throws Exception
    {
        if (isMsBuildPresent())
        {
            MsBuildCommand command = new MsBuildCommand();
            failedRun(command, "Specify a project or solution file");
        }
    }

    public void testBadBuildFile() throws Exception
    {
        if (isMsBuildPresent())
        {
            MsBuildCommand command = new MsBuildCommand();
            command.setBuildFile("nosuchfile");
            failedRun(command, "Project file does not exist");
        }
    }

    public void testCSharpSuccess() throws Exception
    {
        if (isMsBuildPresent())
        {
            createSourceFile();

            copyBuildFileToBaseDir("csharp");
            MsBuildCommand command = new MsBuildCommand();
            successRun(command, "Build succeeded");
        }
    }

    public void testCSharpSuccessCompileError() throws Exception
    {
        if (isMsBuildPresent())
        {
            createSourceFileWithError();

            copyBuildFileToBaseDir("csharp");
            MsBuildCommand command = new MsBuildCommand();
            CommandResult commandResult = failedRun(command, "The name 'i' does not exist in the current context");
            StoredFileArtifact outputArtifact = getOutputArtifact(commandResult);
            assertErrorsMatch(outputArtifact, ".*'i' does not exist.*", "Build FAILED\\.");
        }
    }

    public void testTargets() throws Exception
    {
        if (isMsBuildPresent())
        {
            copyBuildFileToBaseDir("properties");
            MsBuildCommand command = new MsBuildCommand();
            command.setTargets("T1 T2");
            successRun(command, "Ran T1", "Ran T2");
        }
    }

    public void testConfiguration() throws Exception
    {
        if (isMsBuildPresent())
        {
            copyBuildFileToBaseDir("properties");
            MsBuildCommand command = new MsBuildCommand();
            command.setConfiguration("Release");
            successRun(command, "Configuration = Release");
        }
    }

    public void testBuildProperties() throws Exception
    {
        if (isMsBuildPresent())
        {
            copyBuildFileToBaseDir("properties");
            MsBuildCommand command = new MsBuildCommand();
            MsBuildCommand.BuildProperty buildProperty = command.createBuildProperty();
            buildProperty.setName("foo");
            buildProperty.setValue("bar");
            successRun(command, "foo = bar");
        }
    }

    public void testDisablePostProcessing() throws Exception
    {
        if (isMsBuildPresent())
        {
            createSourceFileWithError();
            copyBuildFileToBaseDir("csharp");
            MsBuildCommand command = new MsBuildCommand();
            command.setPostProcess(false);
            CommandResult commandResult = failedRun(command);

            // No post processing, so no error features.
            StoredFileArtifact outputArtifact = getOutputArtifact(commandResult);
            assertErrorsMatch(outputArtifact);
        }
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

    private void createSourceFileWithError() throws IOException
    {
        File sourceFile = new File(baseDir, "Test.cs");
        FileSystemUtils.createFile(sourceFile, "public class Test\n" +
                "{\n" +
                "    static int Main(string[] argv)\n" +
                "    {\n" +
                "        return i;\n" +
                "    }\n" +
                "}");
    }

    protected String getBuildFileName()
    {
        return "build.proj";
    }

    protected String getBuildFileExt()
    {
        return "xml";
    }
}
