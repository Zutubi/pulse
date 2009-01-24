package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.SimpleRecipePaths;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class CommandTestCase extends PulseTestCase
{
    protected File tempDir;
    protected File baseDir;
    protected File outputDir;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getClass().getName() + "." + getName(), ".tmp");
        baseDir = new File(tempDir, "base");
        outputDir = new File(tempDir, "output");
    }

    public void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    protected TestCommandContext runCommand(Command command) throws Exception
    {
        return runCommand(command, new PulseExecutionContext());
    }

    protected TestCommandContext runCommand(Command command, PulseExecutionContext context)
    {
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir);
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, new SimpleRecipePaths(baseDir, outputDir));
        context.setWorkingDir(baseDir);

        TestCommandContext commandContext = new TestCommandContext(context);
        command.execute(commandContext);
        return commandContext;
    }

    private File getFile(String name, String path)
    {
        File dir = new File(outputDir, name);
        return new File(dir, path);
    }

    protected void assertFile(String name, String path)
    {
        File file = getFile(name, path);
        assertTrue("File '" + file.getPath() + "' does not exist", file.exists());   
    }

    protected String getFileContent(String name, String path) throws IOException
    {
        return IOUtils.fileToString(getFile(name, path));
    }

    protected void assertFileContains(String name, String path, String... contents) throws IOException
    {
        assertFileContains(name, path, true, contents);
    }

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

    protected void assertOutputContains(String output, String... contents)
    {
        assertOutputContains(output, true, contents);
    }

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

    protected void assertOutputRegistered(TestCommandContext.Output expectedOutput, TestCommandContext context)
    {
        TestCommandContext.Output gotOutput = context.getOutputs().get(expectedOutput.getName());
        assertNotNull("Expected output '" + expectedOutput.getName() + "' not registered");
        assertEquals(expectedOutput.getIndex(), gotOutput.getIndex());

        List<PostProcessorConfiguration> expectedProcessors = expectedOutput.getAppliedProcessors();
        List<PostProcessorConfiguration> gotProcessors = gotOutput.getAppliedProcessors();
        assertEquals(expectedProcessors.size(), gotProcessors.size());
        for (int i = 0; i < expectedProcessors.size(); i++)
        {
            assertSame(expectedProcessors.get(i), gotProcessors.get(i));   
        }
    }

    protected void assertErrorsMatch(StoredFileArtifact artifact, String... summaryRegexes)
    {
        assertFeatures(artifact, Feature.Level.ERROR, summaryRegexes);
    }

    protected void assertWarningsMatch(StoredFileArtifact artifact, String... summaryRegexes)
    {
        assertFeatures(artifact, Feature.Level.WARNING, summaryRegexes);
    }

    protected void assertFeatures(StoredFileArtifact artifact, Feature.Level level, String... summaryRegexes)
    {
        List<PersistentFeature> features = artifact.getFeatures(level);
        assertEquals(summaryRegexes.length, features.size());
        for(int i = 0; i < summaryRegexes.length; i++)
        {
            assertThat(features.get(i).getSummary(), matchesRegex(summaryRegexes[i]));
        }
    }
}
