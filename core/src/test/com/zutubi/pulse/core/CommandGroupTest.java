package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public class CommandGroupTest extends PulseTestCase
{
    private File baseDirectory;
    private File outputDirectory;

    public void setUp() throws Exception
    {
        super.setUp();
        baseDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), ".base");
        outputDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), ".out");
    }

    public void tearDown() throws Exception
    {
        FileSystemUtils.removeDirectory(outputDirectory);
        FileSystemUtils.removeDirectory(baseDirectory);
        super.tearDown();
    }

    public void testSimpleNestedCommand() throws Exception
    {
        CommandGroup group = createEchoCommand();
        testSuccess(group);
    }

    public void testCaptureFile() throws Exception
    {
        File file = new File(baseDirectory, "testfile");
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

    private void fileCaptureHelper(String file) throws IOException, FileLoadException
    {
        File inFile = new File(baseDirectory, "testfile");
        FileSystemUtils.createFile(inFile, "some data");

        CommandGroup group = createEchoCommand();
        FileArtifact pa = group.createArtifact();
        pa.setName("test-artifact");
        pa.setFile(file);
        CommandResult result = testSuccess(group);

        // Now check the artifact was captured
        StoredArtifact artifact = result.getArtifact("test-artifact");
        assertNotNull(artifact);
        StoredFileArtifact fileArtifact = artifact.getFile();
        File expectedFile = new File(outputDirectory, fileArtifact.getPath());
        assertTrue(expectedFile.isFile());
        assertEquals("some data", IOUtils.fileToString(expectedFile));
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

        CommandResult result = testFailureWithMessage(group, "does not exist");
    }

    public void testCaptureDirNonExistantNoFail() throws Exception
    {
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dri-artifact");
        artifact.setBase(new File("pffft"));
        artifact.setFailIfNotPresent(false);

        CommandResult result = testSuccess(group);
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
        baseDirHelper(new File(baseDirectory, "testdir"));
    }

    public void testBaseDirRelative() throws Exception
    {
        baseDirHelper(new File("testdir"));
    }

    private void baseDirHelper(File base) throws IOException, FileLoadException
    {
        createSomeFiles();
        CommandGroup group = createEchoCommand();
        DirectoryArtifact artifact = group.createDirArtifact();
        artifact.setName("test-dir-artifact");
        artifact.setBase(base);
        CommandResult result = testSuccess(group);

        checkCapturedDir("test-dir-artifact", 3, new File(baseDirectory, "testdir"), result);
    }

    private CommandGroup createEchoCommand() throws FileLoadException
    {
        CommandGroup group = new CommandGroup();
        ExecutableCommand command = new ExecutableCommand();
        command.setWorkingDir(baseDirectory);
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
        File file = new File(baseDirectory, "testfile.txt");
        FileSystemUtils.createFile(file, "some data");
        File nested = new File(baseDirectory, "testdir");
        assertTrue(nested.mkdir());
        File nest1 = new File(nested, "file1.txt");
        FileSystemUtils.createFile(nest1, "file1 data");
        File nest2 = new File(nested, "file2.txt");
        FileSystemUtils.createFile(nest2, "file2 data");
        File nest3 = new File(nested, "file3.foo");
        FileSystemUtils.createFile(nest3, "file3 data");
    }

    private CommandResult testSuccessWithOutput(CommandGroup group, String output) throws IOException
    {
        CommandResult result = new CommandResult("test");
        execute(group, result);
        assertEquals(ResultState.SUCCESS, result.getState());
        StoredArtifact artifact = result.getArtifact(ExecutableCommand.OUTPUT_NAME);
        File outputFile = new File(outputDirectory, artifact.getFile().getPath());
        assertEquals(output, IOUtils.fileToString(outputFile));

        return result;
    }

    private void execute(CommandGroup group, CommandResult result)
    {
        CommandContext context = new CommandContext(new SimpleRecipePaths(baseDirectory, null), outputDirectory);
        group.execute(0, context, result);
    }

    private CommandResult testSuccess(CommandGroup group) throws IOException
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
        checkCapturedDir(name, count, baseDirectory, result);
    }

    private void checkCapturedDir(String name, int count, File base, CommandResult result) throws IOException
    {
        StoredArtifact storedArtifact = result.getArtifact(name);
        assertNotNull(storedArtifact);
        assertEquals(count, storedArtifact.getChildren().size());
        File destDir = new File(outputDirectory, storedArtifact.getName());
        assertTrue(destDir.isDirectory());
        assertDirectoriesEqual(base, destDir);
    }

    private void checkAllButFoo(CommandResult result) throws IOException
    {
        // Check all but testdir/file3.foo was captured
        File file = new File(baseDirectory, FileSystemUtils.composeFilename("testdir", "file3.foo"));
        assertTrue(file.delete());
        checkCapturedDir("test-dir-artifact", 3, result);
    }
}
