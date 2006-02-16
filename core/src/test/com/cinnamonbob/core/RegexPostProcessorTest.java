package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tests for RegexPostProcessor.
 */
public class RegexPostProcessorTest extends BobTestCase
{
    private static final String[] LINES = {"first line",
            "second line",
            "xxx",
            "xxx abc",
            "abc xxx",
            "abc xxx abc"};
    private StoredArtifact artifact;
    private File tempFile;
    private File tempDir;


    public void setUp() throws IOException
    {
        FileArtifact fileArtifact = new FileArtifact();
        fileArtifact.setName("test name");
        fileArtifact.setTitle("test title");
        fileArtifact.setType("text/plain");

        tempFile = File.createTempFile("regex-pp-test", null);
        tempDir = tempFile.getParentFile();
        PrintWriter writer = null;

        try
        {
            writer = new PrintWriter(tempFile.getAbsolutePath());

            for (String line : LINES)
            {
                writer.println(line);
            }
        }
        finally
        {
            IOUtils.close(writer);
        }

        artifact = new StoredArtifact(fileArtifact, tempFile.getName());
    }

    public void tearDown()
    {
        tempFile.delete();
    }

    public void testMatchAll()
    {
        simpleErrors(".*", LINES);
    }

    public void testMatchXXX()
    {
        simpleErrors("xxx", "xxx");
    }

    public void testStartsXXX()
    {
        simpleErrors("xxx.*", "xxx", "xxx abc");
    }

    public void testEndsXXX()
    {
        simpleErrors(".*xxx", "xxx", "abc xxx");
    }

    public void testFloatingXXX()
    {
        simpleErrors(".*xxx.*", "xxx", "xxx abc", "abc xxx", "abc xxx abc");
    }

    public void testCustomSummary()
    {
        RegexPostProcessor pp = new RegexPostProcessor("test-pp");
        RegexPattern pattern = new RegexPattern(Feature.Level.ERROR, Pattern.compile("xxx"));
        pattern.setSummary("custom");
        pp.addRegexPattern(pattern);

        simpleErrors(pp, "custom");
    }

    public void testCustomSummaryGroups()
    {
        RegexPostProcessor pp = new RegexPostProcessor("test-pp");
        RegexPattern pattern = new RegexPattern(Feature.Level.ERROR, Pattern.compile("x(x)x"));
        pattern.setSummary("$1");
        pp.addRegexPattern(pattern);

        simpleErrors(pp, "x");
    }

    public void testExcludeAll()
    {
        RegexPostProcessor pp = createExclusionProcessor(".*xxx.*", ".*");
        simpleErrors(pp);
    }

    public void testExcludeSame()
    {
        RegexPostProcessor pp = createExclusionProcessor(".*xxx", ".*xxx");
        simpleErrors(pp);
    }

    public void testExcludeSome()
    {
        RegexPostProcessor pp = createExclusionProcessor(".*xxx", "xxx");
        simpleErrors(pp, "abc xxx");
    }

    public void testMultipleExclusions()
    {
        RegexPostProcessor pp = createExclusionProcessor(".*", "xxx", ".*abc.*");
        simpleErrors(pp, "first line", "second line");
    }

    public void testFailOnError()
    {
        RegexPostProcessor pp = createPostProcessor(".*");
        CommandResult result = simpleErrors(pp, LINES);
        assertTrue(result.failed());
        assertEquals("Error features detected", result.getFailureMessage());
    }

    public void testNoFailOnError()
    {
        RegexPostProcessor pp = createPostProcessor(".*");
        pp.setFailOnError(false);
        CommandResult result = simpleErrors(pp, LINES);
        assertFalse(result.failed());
    }

    public void testNoFailOnWarning()
    {
        RegexPostProcessor pp = createPostProcessor(".*", Feature.Level.WARNING);
        assertFalse(pp.getFailOnWarning());
        CommandResult result = simpleFeatures(pp, Feature.Level.WARNING, LINES);
        assertFalse(result.failed());
    }

    public void testFailOnWarning()
    {
        RegexPostProcessor pp = createPostProcessor(".*", Feature.Level.WARNING);
        pp.setFailOnWarning(true);
        CommandResult result = simpleFeatures(pp, Feature.Level.WARNING, LINES);
        assertTrue(result.failed());
        assertEquals("Warning features detected", result.getFailureMessage());
    }

    private CommandResult simpleFeatures(RegexPostProcessor pp, Feature.Level level, String... lines)
    {
        CommandResult result = new CommandResult("test");
        pp.process(tempDir, artifact, result);
        List<Feature> features = artifact.getFeatures();

        assertEquals(lines.length, features.size());
        for (int i = 0; i < lines.length; i++)
        {
            Feature feature = features.get(i);
            assertEquals(level, feature.getLevel());
            assertEquals(lines[i], feature.getSummary());
        }

        if (features.size() > 0)
        {
            if (level == Feature.Level.ERROR && pp.getFailOnError())
            {
                assertTrue(result.failed());
                assertEquals("Error features detected", result.getFailureMessage());
            }
            else if (level == Feature.Level.WARNING && pp.getFailOnWarning())
            {
                assertTrue(result.failed());
                assertEquals("Warning features detected", result.getFailureMessage());
            }
        }

        return result;
    }

    private CommandResult simpleErrors(RegexPostProcessor pp, String... lines)
    {
        return simpleFeatures(pp, Feature.Level.ERROR, lines);
    }

    private void simpleErrors(String expression, String... lines)
    {
        RegexPostProcessor pp = createPostProcessor(expression);
        assertTrue(pp.getFailOnError());
        assertFalse(pp.getFailOnWarning());
        simpleErrors(pp, lines);
    }

    private RegexPostProcessor createPostProcessor(String expression)
    {
        return createPostProcessor(expression, Feature.Level.ERROR);
    }

    private RegexPostProcessor createPostProcessor(String expression, Feature.Level level)
    {
        RegexPostProcessor pp = new RegexPostProcessor("test-pp");
        RegexPattern pattern = new RegexPattern(level, Pattern.compile(expression));
        pp.addRegexPattern(pattern);
        return pp;
    }

    private RegexPostProcessor createExclusionProcessor(String expression, String... exclusions)
    {
        RegexPostProcessor pp = new RegexPostProcessor("test-pp");
        RegexPattern pattern = new RegexPattern(Feature.Level.ERROR, Pattern.compile(expression));
        for (String e : exclusions)
        {
            pattern.addExclusion(Pattern.compile(e));
        }
        pp.addRegexPattern(pattern);

        return pp;
    }
}
