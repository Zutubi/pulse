package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.PulseExecutionContext;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorTestBase;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;

import java.io.File;
import java.net.URL;

/**
 */
public class JUnitReportPostProcessorTest extends XMLTestReportPostProcessorTestBase
{
    public JUnitReportPostProcessorTest()
    {
        super(new JUnitReportPostProcessor(new JUnitReportPostProcessorConfiguration()));
    }

    protected File getOutputDir() throws Exception
    {
        URL resource = getClass().getResource("JUnitReportPostProcessorTest.simple.xml");
        return new File(resource.toURI()).getParentFile();
    }

    public void testSimple() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("simple");

        assertEquals(2, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");

        PersistentTestSuiteResult suite = tests.getSuites().get(1);
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        assertEquals(3, children.length);
        checkCase(children[0], "testSimple", PASS, 0, null);
        checkCase(children[1], "testAssertionFailure", FAILURE, 10,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)");
        checkCase(children[2], "testThrowException", ERROR, 10,
                "java.lang.RuntimeException: random message\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)");
    }

    public void testSingle() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("single");
        assertSingleSuite(tests);
    }

    public void testSuite() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("suite");
        assertEquals(274, tests.getTotal());
        assertNotNull(tests.getSuite("com.zutubi.pulse.acceptance.AcceptanceTestSuite").getCase("com.zutubi.pulse.acceptance.LicenseAuthorisationAcceptanceTest.testAddProjectLinkOnlyAvailableWhenLicensed"));
    }

    public void testCustom() throws Exception
    {
        JUnitReportPostProcessorConfiguration pp = (JUnitReportPostProcessorConfiguration) this.pp.getConfig();
        pp.setSuiteElement("customtestsuite");
        pp.setCaseElement("customtestcase");
        pp.setFailureElement("customfailure");
        pp.setErrorElement("customerror");
        pp.setSkippedElement("customskipped");
        pp.setNameAttribute("customname");
        pp.setPackageAttribute("custompackage");
        pp.setTimeAttribute("customtime");
        PersistentTestSuiteResult tests = runProcessor("custom");
        assertSingleSuite(tests);
    }

    private void assertSingleSuite(PersistentTestSuiteResult tests)
    {
        assertEquals(1, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest", suite.getName());
        assertEquals(391, suite.getDuration());

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        assertEquals(4, children.length);
        checkCase(children[0], "testSimple", PASS, 291, null);
        checkCase(children[1], "testSkipped", SKIPPED, 0, null);
        checkCase(children[2], "testFailure", FAILURE, 10,
                "junit.framework.AssertionFailedError\n" +
                        "\tat\n" +
                        "        com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest.testFailure(JUnitReportPostProcessorTest.java:63)");
        checkCase(children[3], "testError", ERROR, 0,
                "java.lang.RuntimeException: whoops!\n" +
                        "\tat\n" +
                        "        com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest.testError(JUnitReportPostProcessorTest.java:68)");
    }

    public void testFailOnFailure() throws Exception
    {
        CommandResult result = new CommandResult("test");
        result.commence();
        failOnFailureHelper(result);

        assertTrue(result.failed());
        assertEquals("One or more test cases failed.", result.getFeatures().get(0).getSummary());
    }

    public void testFailOnFailureNotSet() throws Exception
    {
        CommandResult result = new CommandResult("test");
        result.commence();
        pp.getConfig().setFailOnFailure(false);
        failOnFailureHelper(result);

        assertFalse(result.failed());
        assertEquals(0, result.getFeatures().size());
    }

    public void testFailOnFailureAlreadyFailed() throws Exception
    {
        CommandResult result = new CommandResult("test");
        result.commence();
        result.failure("bogosity");
        failOnFailureHelper(result);

        assertTrue(result.failed());
        assertEquals(1, result.getFeatures().size());
        assertEquals("bogosity", result.getFeatures().get(0).getSummary());
    }

    public void testNoMessage() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("nomessage");
        PersistentTestSuiteResult suite = tests.getSuite("com.zutubi.pulse.junit.NoMessages");
        checkSuite(suite, "com.zutubi.pulse.junit.NoMessages", 2, 2, 0);
        checkFailureCase(suite, "testFailureNoMessageAtAll", null);
        checkFailureCase(suite, "testFailureMessageInAttribute", "this message only");
    }

    public void testNested() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("nested");
        PersistentTestSuiteResult suite = tests.getSuite("Outer");
        assertNotNull(suite);
        checkSuite(suite, "Outer", 2, 0, 0);
        PersistentTestSuiteResult nested = suite.getSuite("Nested");
        checkSuite(nested, "Nested", 2, 0, 0);
        checkPassCase(nested, "test1");
        checkPassCase(nested, "test2");
    }

    public void testNoSuiteName() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("nonamesuite");
        assertEquals(0, tests.getSuites().size());
    }

    private void checkWarning(PersistentTestResult testResult, String name, long duration, String contents)
    {
        assertTrue(testResult instanceof PersistentTestSuiteResult);
        PersistentTestSuiteResult suite = (PersistentTestSuiteResult) testResult;
        assertEquals(name, suite.getName());
        assertEquals(duration, suite.getDuration());

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        assertEquals(1, children.length);
        PersistentTestCaseResult caseResult = children[0];
        assertEquals("warning", caseResult.getName());
        assertEquals(10, caseResult.getDuration());
        assertTrue(caseResult.getMessage().contains(contents));
    }

    private void failOnFailureHelper(CommandResult result) throws Exception
    {
        File outputDir = getOutputDir();
        StoredFileArtifact artifact = getArtifact("simple");
        PersistentTestSuiteResult testResults = new PersistentTestSuiteResult();

        ExecutionContext context = new PulseExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());

        pp.process(new File(outputDir, artifact.getPath()), new DefaultPostProcessorContext(artifact, result, context));
    }
}
