package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;

import java.io.File;
import java.net.URL;

/**
 */
public class JUnitReportPostProcessorTest extends XMLReportPostProcessorTestBase
{
    public JUnitReportPostProcessorTest()
    {
        this(null);
    }

    public JUnitReportPostProcessorTest(String name)
    {
        super(name, new JUnitReportPostProcessor());
    }

    protected File getOutputDir()
    {
        URL resource = getClass().getResource("JUnitReportPostProcessorTest.simple.xml");
        return new File(resource.getPath()).getParentFile();
    }

    public void testSimple()
    {
        TestSuiteResult tests = runProcessor("simple");

        assertEquals(3, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");
        checkWarning(tests.getSuites().get(1), "com.zutubi.pulse.junit.NonConstructableTest", 100, "no public constructor");

        TestSuiteResult suite = tests.getSuites().get(2);
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(3, children.length);
        checkCase(children[0], "testSimple", TestCaseResult.Status.PASS, 0, null);
        checkCase(children[1], "testAssertionFailure", TestCaseResult.Status.FAILURE, 10,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)");
        checkCase(children[2], "testThrowException", TestCaseResult.Status.ERROR, 10,
                "java.lang.RuntimeException: random message\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)");
    }

    public void testSingle()
    {
        TestSuiteResult tests = runProcessor("single");

        assertEquals(1, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("com.zutubi.pulse.core.JUnitReportPostProcessorTest", suite.getName());
        assertEquals(391, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(3, children.length);
        checkCase(children[0], "testSimple", TestCaseResult.Status.PASS, 291, null);
        checkCase(children[1], "testFailure", TestCaseResult.Status.FAILURE, 10,
                "junit.framework.AssertionFailedError\n" +
                        "\tat com.zutubi.pulse.core.JUnitReportPostProcessorTest.testFailure(JUnitReportPostProcessorTest.java:63)");
        checkCase(children[2], "testError", TestCaseResult.Status.ERROR, 0,
                "java.lang.RuntimeException: whoops!\n" +
                        "\tat com.zutubi.pulse.core.JUnitReportPostProcessorTest.testError(JUnitReportPostProcessorTest.java:68)");
    }

    public void testFailOnFailure()
    {
        CommandResult result = new CommandResult("test");
        result.commence();
        failOnFailureHelper(result);

        assertTrue(result.failed());
        assertEquals("One or more test cases failed.", result.getFeatures().get(0).getSummary());
    }

    public void testFailOnFailureNotSet()
    {
        CommandResult result = new CommandResult("test");
        result.commence();
        pp.setFailOnFailure(false);
        failOnFailureHelper(result);

        assertFalse(result.failed());
        assertEquals(0, result.getFeatures().size());
    }

    public void testFailOnFailureAlreadyFailed()
    {
        CommandResult result = new CommandResult("test");
        result.commence();
        result.failure("bogosity");
        failOnFailureHelper(result);

        assertTrue(result.failed());
        assertEquals(1, result.getFeatures().size());
        assertEquals("bogosity", result.getFeatures().get(0).getSummary());
    }

    private void checkWarning(TestResult testResult, String name, long duration, String contents)
    {
        assertTrue(testResult instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) testResult;
        assertEquals(name, suite.getName());
        assertEquals(duration, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(1, children.length);
        TestCaseResult caseResult = children[0];
        assertEquals("warning", caseResult.getName());
        assertEquals(10, caseResult.getDuration());
        assertTrue(caseResult.getMessage().contains(contents));
    }

    private void failOnFailureHelper(CommandResult result)
    {
        File outputDir = getOutputDir();
        StoredFileArtifact artifact = getArtifact("simple");
        TestSuiteResult testResults = new TestSuiteResult();

        RecipeContext recipeContext = new RecipeContext();
        recipeContext.setTestResults(testResults);

        CommandContext context = new CommandContext();
        context.setRecipeContext(recipeContext);
        context.setOutputDir(outputDir);

        pp.process(artifact, result, context);
    }
}
