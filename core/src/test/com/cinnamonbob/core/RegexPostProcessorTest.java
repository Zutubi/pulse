package com.cinnamonbob.core;

import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
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
        assertEquals("Error features detected", getFailureMessage(result));
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

    private void contextHelper(int leading, int trailing)
    {
        RegexPostProcessor pp = createPostProcessor(".*");
        pp.setLeadingContext(leading);
        pp.setTrailingContext(trailing);
        simpleErrors(pp, joinLines(leading, trailing));
        checkFeatureLines(artifact, leading, trailing);
    }

    private void checkFeatureLines(StoredFileArtifact artifact, int leading, int trailing)
    {
        int lineNumber = 1;
        for (Feature f : artifact.getFeatures())
        {
            PlainFeature pf = (PlainFeature) f;
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

        System.out.println("result = " + Arrays.asList(result));
        return result;
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
                assertEquals("Error features detected", getFailureMessage(result));
            }
            else if (level == Feature.Level.WARNING && pp.getFailOnWarning())
            {
                assertTrue(result.failed());
                assertEquals("Warning features detected", getFailureMessage(result));
            }
        }

        return result;
    }

    private String getFailureMessage(Result result)
    {
        Feature feature = result.getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        return feature.getSummary();
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
