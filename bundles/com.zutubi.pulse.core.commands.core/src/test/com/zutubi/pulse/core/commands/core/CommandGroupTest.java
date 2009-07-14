package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.DirectoryArtifact;
import com.zutubi.pulse.core.FileArtifact;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.SimpleRecipePaths;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.test.IOAssertions;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

public class CommandGroupTest extends CommandTestBase
{
    public void testSimpleNestedCommand() throws Exception
    {
        CommandGroup group = createEchoCommand();
        testSuccess(group);
    }

    public void testCaptureFile() throws Exception
    {
        File file = new File(baseDir, "testfile");
        fileCaptureHelper(file.getAbsolutePath());
    }

    public void testCaptureFileRelative() throws Exception
    {
        fileCaptureHelper("testfile");
    }

    public void testCaptureFileNonExistent() throws Exception
    {
        CommandGroup group = createEchoCommand();
        FileArtifact fa = group.createArtifact();
        fa.setName("non-existent");
        fa.setFile("pfffft");
        testFailureWithMessage(group, "no file matching");
    }

    public void testCaptureFileNonExistentNoFail() throws Exception
    {
        CommandGroup group = createEchoCommand();
        FileArtifact fa = group.createArtifact();
        fa.setName("non-existent");
        fa.setFile("pfffft");
        fa.setFailIfNotPresent(false);
        testSuccess(group);
    }

    private void fileCaptureHelper(String file) throws Exception
    {
        File inFile = new File(baseDir, "testfile");
        FileSystemUtils.createFile(inFile, "some data");

        CommandGroup group = createEchoCommand();
        FileArtifact pa = group.createArtifact();
        pa.setName("test-artifact");
        pa.setFile(file);
        CommandResult result = testSuccess(group);

        // Now check the artifact was captured
        checkArtifact(result, result.getArtifact("test-artifact"), "some data");
    }

    public void testCaptureDir() throws Exception
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        dirHelper(group, "test-dir-artifact");

        CommandResult result = testSuccess(group);
        // Check the whole directory was captured
        checkCapturedDir("test-dir-artifact", 4, result);
    }

    public void testCaptureDirNonExistant() throws Exception
    {
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dri-artifact");
        artifact.setBase(new File("pffft"));

        testFailureWithMessage(group, "does not exist");
    }

    public void testCaptureDirNonExistantNoFail() throws Exception
    {
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dri-artifact");
        artifact.setBase(new File("pffft"));
        artifact.setFailIfNotPresent(false);

        testSuccess(group);
    }
    
    public void testMultiDirCapture() throws Exception
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        dirHelper(group, "test-dir-artifact");
        dirHelper(group, "test-dir-artifact2");

        CommandResult result = testSuccess(group);

        // Check the whole directory was captured twice
        checkCapturedDir("test-dir-artifact", 4, result);
        checkCapturedDir("test-dir-artifact2", 4, result);
    }

    private void dirHelper(CommandGroup group, String name) throws IOException
    {
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName(name);
    }

    public void testDirIncludes() throws Exception
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dir-artifact");
        artifact.createInclude().setPattern("**/*.txt");
        CommandResult result = testSuccess(group);

        checkAllButFoo(result);
    }

    public void testDirExcludes() throws Exception
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dir-artifact");
        artifact.createExclude().setPattern("**/*.foo");
        CommandResult result = testSuccess(group);

        checkAllButFoo(result);
    }

    public void testIncludesAndExcludes() throws Exception
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dir-artifact");
        artifact.createInclude().setPattern("**/*file*");
        artifact.createExclude().setPattern("**/*.foo");
        CommandResult result = testSuccess(group);

        checkAllButFoo(result);
    }

    public void testBaseDir() throws Exception
    {
        baseDirHelper(new File(baseDir, "testdir"));
    }

    public void testBaseDirRelative() throws Exception
    {
        baseDirHelper(new File("testdir"));
    }

    private void baseDirHelper(File base) throws Exception
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dir-artifact");
        artifact.setBase(base);
        CommandResult result = testSuccess(group);

        checkCapturedDir("test-dir-artifact", 3, new File(baseDir, "testdir"), result);
    }

    private CommandGroup createEchoCommand() throws FileLoadException
    {
        CommandGroup group = new CommandGroup();
        ExecutableCommand command = new ExecutableCommand();
        command.setWorkingDir(baseDir);
        command.setExe("echo");
        command.setArgs("hello world");
        group.add(command);
        return group;
    }

    private void createSomeFiles() throws IOException
    {
        // Create some files:
        // ${base.dir}/
        //   testfile.txt
        //   testdir/
        //     file1.txt
        //     file2.txt
        //     file3.foo
        File file = new File(baseDir, "testfile.txt");
        FileSystemUtils.createFile(file, "some data");
        File nested = new File(baseDir, "testdir");
        assertTrue(nested.mkdir());
        File nest1 = new File(nested, "file1.txt");
        FileSystemUtils.createFile(nest1, "file1 data");
        File nest2 = new File(nested, "file2.txt");
        FileSystemUtils.createFile(nest2, "file2 data");
        File nest3 = new File(nested, "file3.foo");
        FileSystemUtils.createFile(nest3, "file3 data");
    }

    private CommandResult testSuccessWithOutput(CommandGroup group, String output) throws Exception
    {
        CommandResult result = runCommand(group);
        assertEquals(ResultState.SUCCESS, result.getState());
        checkArtifact(result, result.getArtifact(ExecutableCommand.OUTPUT_ARTIFACT_NAME), output);

        return result;
    }

    private void execute(CommandGroup group, CommandResult result)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, new SimpleRecipePaths(baseDir, null));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());
        context.setWorkingDir(baseDir);

        group.execute(context, result);
    }

    private CommandResult testSuccess(CommandGroup group) throws Exception
    {
        return testSuccessWithOutput(group, "hello world\n");
    }

    private CommandResult testFailureWithMessage(CommandGroup group, String message) throws IOException
    {
        CommandResult result = null;
        try
        {
            result = new CommandResult("test");
            execute(group, result);
        }
        catch (BuildException e)
        {
            assertTrue(e.getMessage().contains(message));
        }

        return result;
    }

    private void checkCapturedDir(String name, int count, CommandResult result) throws IOException
    {
        checkCapturedDir(name, count, baseDir, result);
    }

    private void checkCapturedDir(String name, int count, File base, CommandResult result) throws IOException
    {
        StoredArtifact storedArtifact = result.getArtifact(name);
        assertNotNull(storedArtifact);
        assertEquals(count, storedArtifact.getChildren().size());

        String commandDirName = String.format("00000000-%s", result.getCommandName());
        File destDir = new File(outputDir, FileSystemUtils.composeFilename(commandDirName, storedArtifact.getName()));
        assertTrue(destDir.isDirectory());
        IOAssertions.assertDirectoriesEqual(base, destDir);
    }

    private void checkAllButFoo(CommandResult result) throws IOException
    {
        // Check all but testdir/file3.foo was captured
        File file = new File(baseDir, FileSystemUtils.composeFilename("testdir", "file3.foo"));
        assertTrue(file.delete());
        checkCapturedDir("test-dir-artifact", 3, result);
    }


    protected String getBuildFileName()
    {
        return null;
    }

    protected String getBuildFileExt()
    {
        return null;
    }
}
