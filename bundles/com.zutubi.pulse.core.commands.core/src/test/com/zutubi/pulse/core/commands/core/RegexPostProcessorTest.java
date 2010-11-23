package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RegexPatternConfiguration;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.marshal.FileLoadException;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tests for RegexPostProcessor.
 */
public class RegexPostProcessorTest extends PulseTestCase
{
    private static final String[] LINES = {"first line",
            "second line",
            "xxx",
            "xxx abc",
            "abc xxx",
            "abc xxx abc"};
    private StoredFileArtifact artifact;
    private File tempFile;
    private File tempDir;


    public void setUp() throws IOException
    {
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

        artifact = new StoredFileArtifact(tempFile.getName());
    }

    public void tearDown()
    {
        assertTrue(tempFile.delete());
    }

    public void testMatchAll()
    {
        simpleErrors(".*", LINES);
    }

    public void testMatchXXX()
    {
        simpleErrors("^xxx$", "xxx");
    }

    public void testStartsXXX()
    {
        simpleErrors("^xxx.*", "xxx", "xxx abc");
    }

    public void testEndsXXX()
    {
        simpleErrors(".*xxx$", "xxx", "abc xxx");
    }

    public void testFloatingXXX()
    {
        simpleErrors("xxx", "xxx", "xxx abc", "abc xxx", "abc xxx abc");
    }

    public void testCustomSummary()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("test-pp");
        RegexPatternConfiguration pattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("^xxx$"));
        pattern.setSummary("custom");
        pp.getPatterns().add(pattern);

        simpleErrors(pp, "custom");
    }

    public void testCustomSummaryGroups()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("test-pp");
        RegexPatternConfiguration pattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("^x(x)x$"));
        pattern.setSummary("$1");
        pp.getPatterns().add(pattern);

        simpleErrors(pp, "x");
    }

    public void testExcludeAll()
    {
        RegexPostProcessorConfiguration pp = createExclusionProcessor("xxx", ".*");
        simpleErrors(pp);
    }

    public void testExcludeSame()
    {
        RegexPostProcessorConfiguration pp = createExclusionProcessor(".*xxx$", ".*xxx$");
        simpleErrors(pp);
    }

    public void testExcludeSome()
    {
        RegexPostProcessorConfiguration pp = createExclusionProcessor(".*xxx$", "^xxx$");
        simpleErrors(pp, "abc xxx");
    }

    public void testMultipleExclusions()
    {
        RegexPostProcessorConfiguration pp = createExclusionProcessor(".*", "^xxx$", "abc");
        simpleErrors(pp, "first line", "second line");
    }

    public void testFailOnError()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(".*");
        CommandResult result = simpleErrors(pp, LINES);
        assertTrue(result.failed());
        assertEquals("Error features detected", getFailureMessage(result));
    }

    public void testNoFailOnError()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(".*");
        pp.setFailOnError(false);
        CommandResult result = simpleErrors(pp, LINES);
        assertFalse(result.failed());
    }

    public void testNoFailOnWarning()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(".*", Feature.Level.WARNING);
        assertFalse(pp.isFailOnWarning());
        CommandResult result = simpleFeatures(pp, Feature.Level.WARNING, LINES);
        assertFalse(result.failed());
    }

    public void testFailOnWarning()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(".*", Feature.Level.WARNING);
        pp.setFailOnWarning(true);
        CommandResult result = simpleFeatures(pp, Feature.Level.WARNING, LINES);
        assertTrue(result.failed());
        assertEquals("Warning features detected", getFailureMessage(result));
    }

    public void testLeadingContext()
    {
        contextHelper(1, 0);
    }

    public void testTrailingContext()
    {
        contextHelper(0, 1);
    }

    public void testLeadingAndTrailingContext()
    {
        contextHelper(1, 1);
    }

    public void testMultipleLeadingContext()
    {
        contextHelper(2, 0);
    }

    public void testMultipleTrailingContext()
    {
        contextHelper(0, 2);
    }

    public void testMultipleLeadingAndTrailingContext()
    {
        contextHelper(2, 2);
    }

    public void testJustUnderLeadingContext()
    {
        contextHelper(LINES.length - 1, 0);
    }

    public void testJustUnderTrailingContext()
    {
        contextHelper(0, LINES.length - 1);
    }

    public void testJustUnderLEadingAndTrailingContext()
    {
        contextHelper(LINES.length - 1, LINES.length - 1);
    }

    public void testExactLeadingContext()
    {
        contextHelper(LINES.length, 0);
    }

    public void testExactTrailingContext()
    {
        contextHelper(0, LINES.length);
    }

    public void testExactLeadingAndTrailingContext()
    {
        contextHelper(LINES.length, LINES.length);
    }

    public void testJustOverLeadingContext()
    {
        contextHelper(LINES.length + 1, 0);
    }

    public void testJustOverTrailingContext()
    {
        contextHelper(0, LINES.length + 1);
    }

    public void testJustOverLeadingAndTrailingContext()
    {
        contextHelper(LINES.length + 1, LINES.length + 1);
    }

    public void testHugeLeadingContext()
    {
        contextHelper(10000, 0);
    }

    public void testHugeTrailingContext()
    {
        contextHelper(0, 10000);
    }

    public void testHugeLeadingAndTrailingContext()
    {
        contextHelper(10000, 10000);
    }

    public void testJoinOverlapping()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor("xxx");
        pp.setTrailingContext(1);
        simpleErrors(pp, "xxx\nxxx abc\nabc xxx\nabc xxx abc");
    }

    public void testJoinDoubleOverlapping()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor("xxx");
        pp.setTrailingContext(2);
        simpleErrors(pp, "xxx\nxxx abc\nabc xxx\nabc xxx abc");
    }

    public void testNotJoinAdjacent()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor("xxx abc");
        pp.setTrailingContext(1);
        simpleErrors(pp, "xxx abc\nabc xxx", "abc xxx abc");
    }

    public void testJoinSeparated()
    {
        RegexPostProcessorConfiguration pp = createPostProcessor("xxx abc");
        pp.setTrailingContext(2);
        simpleErrors(pp, "xxx abc\nabc xxx\nabc xxx abc");
    }

    public void testOverlappingDifferentLevels() throws FileLoadException
    {
        RegexPostProcessorConfiguration ppConfig = new RegexPostProcessorConfiguration("test");
        RegexPatternConfiguration pattern = new RegexPatternConfiguration();
        pattern.setCategory(Feature.Level.WARNING);
        pattern.setExpression("^xxx abc$");
        ppConfig.setTrailingContext(1);
        ppConfig.getPatterns().add(pattern);

        pattern = new RegexPatternConfiguration();
        pattern.setCategory(Feature.Level.ERROR);
        pattern.setExpression("^abc xxx$");
        ppConfig.setLeadingContext(1);
        ppConfig.getPatterns().add(pattern);

        CommandResult result = new CommandResult("test");
        ExecutionContext context = new PulseExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tempDir.getAbsolutePath());

        RegexPostProcessor pp = new RegexPostProcessor(ppConfig);
        pp.process(tempFile, new DefaultPostProcessorContext(artifact, result, Integer.MAX_VALUE, context));
        List<PersistentFeature> features = artifact.getFeatures();
        assertEquals(2, features.size());
        assertEquals(Feature.Level.WARNING, features.get(0).getLevel());
        assertEquals(Feature.Level.ERROR, features.get(1).getLevel());
    }

    public void testSmileFace() throws FileLoadException, FileNotFoundException
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(":-\\)");

        writeToArtifact("first line", ":-) blah blah blah", "last line");

        simpleFeatures(pp, Feature.Level.ERROR, ":-) blah blah blah");
    }

    private void writeToArtifact(String... lines) throws FileNotFoundException
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(tempFile.getAbsolutePath());

            for (String line : lines)
            {
                writer.println(line);
            }
        }
        finally
        {
            IOUtils.close(writer);
        }

        artifact = new StoredFileArtifact(tempFile.getName());

    }

    private void contextHelper(int leading, int trailing)
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(".*");
        pp.setLeadingContext(leading);
        pp.setTrailingContext(trailing);
        pp.setJoinOverlapping(false);
        simpleErrors(pp, joinLines(leading, trailing));
        checkFeatureLines(artifact, leading, trailing);
    }

    private void checkFeatureLines(StoredFileArtifact artifact, int leading, int trailing)
    {
        int lineNumber = 1;
        for (PersistentFeature f : artifact.getFeatures())
        {
            PersistentPlainFeature pf = (PersistentPlainFeature) f;
            assertEquals(lineNumber, pf.getLineNumber());
            assertEquals(lineNumber > leading ? lineNumber - leading : 1, pf.getFirstLine());
            assertEquals(lineNumber + trailing <= LINES.length ? lineNumber + trailing : LINES.length, pf.getLastLine());
            lineNumber++;
        }
    }

    private String[] joinLines(int leading, int trailing)
    {
        String[] result = new String[LINES.length];

        for (int i = 0; i < LINES.length; i++)
        {
            StringBuilder joined = new StringBuilder();
            for (int j = i - leading; j < i; j++)
            {
                if (j >= 0)
                {
                    joined.append(LINES[j]);
                    joined.append('\n');
                }
            }

            joined.append(LINES[i]);

            for (int j = i + 1; j <= i + trailing && j < LINES.length; j++)
            {
                joined.append('\n');
                joined.append(LINES[j]);
            }

            result[i] = joined.toString();
        }

        return result;
    }

    private CommandResult simpleFeatures(RegexPostProcessorConfiguration config, Feature.Level level, String... lines)
    {
        CommandResult result = new CommandResult("test");
        ExecutionContext context = new PulseExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tempDir.getAbsolutePath());
        RegexPostProcessor pp = new RegexPostProcessor(config);
        pp.process(tempFile, new DefaultPostProcessorContext(artifact, result, Integer.MAX_VALUE, context));
        List<PersistentFeature> features = artifact.getFeatures();

        assertEquals(lines.length, features.size());
        for (int i = 0; i < lines.length; i++)
        {
            PersistentFeature feature = features.get(i);
            assertEquals(level, feature.getLevel());
            assertEquals(lines[i], feature.getSummary());
        }

        if (features.size() > 0)
        {
            if (level == Feature.Level.ERROR && config.isFailOnError())
            {
                assertTrue(result.failed());
                assertEquals("Error features detected", getFailureMessage(result));
            }
            else if (level == Feature.Level.WARNING && config.isFailOnWarning())
            {
                assertTrue(result.failed());
                assertEquals("Warning features detected", getFailureMessage(result));
            }
        }

        return result;
    }

    private String getFailureMessage(Result result)
    {
        PersistentFeature feature = result.getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        return feature.getSummary();
    }

    private CommandResult simpleErrors(RegexPostProcessorConfiguration pp, String... lines)
    {
        return simpleFeatures(pp, Feature.Level.ERROR, lines);
    }

    private void simpleErrors(String expression, String... lines)
    {
        RegexPostProcessorConfiguration pp = createPostProcessor(expression);
        assertTrue(pp.isFailOnError());
        assertFalse(pp.isFailOnWarning());
        simpleErrors(pp, lines);
    }

    private RegexPostProcessorConfiguration createPostProcessor(String expression)
    {
        return createPostProcessor(expression, Feature.Level.ERROR);
    }

    private RegexPostProcessorConfiguration createPostProcessor(String expression, Feature.Level level)
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("test-pp");
        pp.addRegexes(level, expression);
        return pp;
    }

    private RegexPostProcessorConfiguration createExclusionProcessor(String expression, String... exclusions)
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("test-pp");
        RegexPatternConfiguration pattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile(expression));
        for (String e : exclusions)
        {
            pattern.getExclusions().add(e);
        }
        pp.getPatterns().add(pattern);

        return pp;
    }
}
