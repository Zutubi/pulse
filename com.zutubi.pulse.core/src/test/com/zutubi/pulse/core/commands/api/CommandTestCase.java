package com.zutubi.pulse.core.commands.api;

import com.google.common.io.Files;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.SimpleRecipePaths;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Helper base class for implementing test cases for commands.
 */
public abstract class CommandTestCase extends PulseTestCase
{
    protected File tempDir;
    protected File baseDir;
    protected File outputDir;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        tempDir = createTempDirectory();
        baseDir = new File(tempDir, "base");
        assertTrue(baseDir.mkdir());
        outputDir = new File(tempDir, "output");
        assertTrue(outputDir.mkdir());
    }

    @Override
    public void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    /**
     * Creates a minimal execution context to use as part of the command
     * context.
     * 
     * @return a new, minimal execution context
     */
    protected ExecutionContext createExecutionContext()
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir);
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, new SimpleRecipePaths(baseDir, outputDir));
        context.setWorkingDir(baseDir);
        return context;
    }

    /**
     * Runs the given command with a minimal execution context, returning a
     * test context recording the results.
     *
     * @param command the command to run
     * @return a test context that can be inspected to determine the results
     *         of running the command
     * @throws Exception on any error
     */
    protected TestCommandContext runCommand(Command command) throws Exception
    {
        return runCommand(command, createExecutionContext());
    }

    /**
     * Runs the given command with the given execution context, returning a
     * test context recording the results.
     *
     * @param command the command to run
     * @param context the context in which to execute the command
     * @return a test context that can be inspected to determine the results
     *         of running the command
     */
    protected TestCommandContext runCommand(Command command, ExecutionContext context)
    {
        TestCommandContext commandContext = new TestCommandContext(context);
        try
        {
            command.execute(commandContext);
            return commandContext;
        }
        finally
        {
            commandContext.complete();
        }
    }

    /**
     * Gets a file object pointing at the file within the given artifact with the
     * given path.
     *
     * @param name the name of the registered artifact the file should have been
     *             captured in
     * @param path the path of the file under the artifact directory
     * @return a file object pointing to the given file in the given artifact
     */
    protected File getFile(String name, String path)
    {
        File dir = new File(outputDir, name);
        return new File(dir, path);
    }

    /**
     * Asserts a file exists (was captured during execution) in the given
     * artifact with the given path.
     *
     * @param name the name of the registered artifact the file should have been
     *             captured in
     * @param path the path of the file under the artifact directory to test for
     */
    protected void assertFileExists(String name, String path)
    {
        File file = getFile(name, path);
        assertTrue("File '" + file.getPath() + "' does not exist", file.exists());   
    }

    /**
     * Gets the contents of a file captured in the given artifact with the given
     * path.
     *
     * @param name the name of the registered artifact the file should have been
     *             captured in
     * @param path the path of the file under the artifact directory
     * @return the entire contents of the file
     * @throws java.io.IOException if there is an error reading the file
     */
    protected String getFileContent(String name, String path) throws IOException
    {
        return Files.toString(getFile(name, path), Charset.defaultCharset());
    }

    /**
     * Asserts that the file captured in the given artifact with the given path
     * contains all of the given strings.  The order of the strings is not
     * important.
     *
     * @param name     the name of the registered artifact the file should have
     *                 been captured in
     * @param path     the path of the file under the artifact directory
     * @param contents the strings to test for in the file
     * @throws IOException if there is an error reading the file
     */
    protected void assertFileContains(String name, String path, String... contents) throws IOException
    {
        assertFileContains(name, path, true, contents);
    }

    /**
     * Asserts that the file captured in the given artifact with the given path
     * contains all of the given strings, possibly case-insensitively.  The
     * order of the strings is not important.
     *
     * @param name          the name of the registered artifact the file should
     *                      have been captured in
     * @param path          the path of the file under the artifact directory
     * @param caseSensitive if true, the search is case-insensitive
     * @param contents the strings to test for in the file
     * @throws IOException if there is an error reading the file
     */
    protected void assertFileContains(String name, String path, boolean caseSensitive, String... contents) throws IOException
    {
        String output = getFileContent(name, path);
        if (!caseSensitive)
        {
            output = output.toLowerCase();
        }

        for (String content: contents)
        {
            if (!caseSensitive)
            {
                content = content.toLowerCase();
            }

            assertThat(output, containsString(content));
        }
    }

    /**
     * Asserts the given output string contains each of the given content
     * strings.  The order of the strings is not important.
     *
     * @param output   the output string to search within
     * @param contents the strings to search for
     */
    protected void assertOutputContains(String output, String... contents)
    {
        assertOutputContains(output, true, contents);
    }

    /**
     * Asserts the given output string contains each of the given content
     * strings, possibly case-insensitively.  The order of the strings is
     * not important.
     *
     * @param output        the output string to search within
     * @param caseSensitive if true, the search is case-insensitive
     * @param contents      the strings to search for
     */
    protected void assertOutputContains(String output, boolean caseSensitive, String... contents)
    {
        if (!caseSensitive)
        {
            output = output.toLowerCase();
        }
        for (String content : contents)
        {
            if (!caseSensitive)
            {
                content = content.toLowerCase();
            }
            if (!output.contains(content))
            {
                fail("Output '" + output + "' does not contain '" + content + "'");
            }
        }
    }

    /**
     * Asserts that an artifact matching the given expected artifact has been
     * registered with the given context.  Used to check that execution of a
     * command registered an expected artifact.
     *
     * @param expectedArtifact the expected artifact, including post-processors
     *                       that should be registered against it
     * @param context        context from the command execution
     */
    protected void assertArtifactRegistered(TestCommandContext.Artifact expectedArtifact, TestCommandContext context)
    {
        TestCommandContext.Artifact gotArtifact = context.getArtifacts().get(expectedArtifact.getName());
        assertNotNull("Expected artifact '" + expectedArtifact.getName() + "' not registered");
        assertEquals(expectedArtifact.getIndex(), gotArtifact.getIndex());

        List<PostProcessorConfiguration> expectedProcessors = expectedArtifact.getAppliedProcessors();
        List<PostProcessorConfiguration> gotProcessors = gotArtifact.getAppliedProcessors();
        assertEquals(expectedProcessors.size(), gotProcessors.size());
        for (int i = 0; i < expectedProcessors.size(); i++)
        {
            assertSame(expectedProcessors.get(i), gotProcessors.get(i));   
        }
    }
}
