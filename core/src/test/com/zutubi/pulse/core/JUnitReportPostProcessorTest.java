package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;

import java.io.File;

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

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSimple()
    {
        TestSuiteResult tests = runProcessor("simple");

        assertEquals(2, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");

        TestSuiteResult suite = tests.getSuites().get(1);
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
        assertSingleSuite(tests);
    }

    public void testSuite()
    {
        TestSuiteResult tests = runProcessor("suite");
        assertEquals(274, tests.getTotal());
        assertNotNull(tests.getSuite("com.zutubi.pulse.acceptance.AcceptanceTestSuite").getCase("com.zutubi.pulse.acceptance.LicenseAuthorisationAcceptanceTest.testAddProjectLinkOnlyAvailableWhenLicensed"));
    }

    public void testCustom()
    {
        JUnitReportPostProcessor pp = (JUnitReportPostProcessor) this.pp;
        pp.setSuiteElement("customtestsuite");
        pp.setCaseElement("customtestcase");
        pp.setFailureElement("customfailure");
        pp.setErrorElement("customerror");
        pp.setNameAttribute("customname");
        pp.setPackageAttribute("custompackage");
        pp.setTimeAttribute("customtime");
        TestSuiteResult tests = runProcessor("custom");
        assertSingleSuite(tests);
    }

    private void assertSingleSuite(TestSuiteResult tests)
    {
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

    public void testNoMessage()
    {
        TestSuiteResult tests = runProcessor("nomessage");
        TestSuiteResult suite = tests.getSuite("com.zutubi.pulse.junit.NoMessages");
        checkSuite(suite, "com.zutubi.pulse.junit.NoMessages", 2, 2, 0);
        checkFailureCase(suite, "testFailureNoMessageAtAll", null);
        checkFailureCase(suite, "testFailureMessageInAttribute", "this message only");
    }

    public void testNested()
    {
        TestSuiteResult tests = runProcessor("nested");
        TestSuiteResult suite = tests.getSuite("Outer");
        assertNotNull(suite);
        checkSuite(suite, "Outer", 2, 0, 0);
        TestSuiteResult nested = suite.getSuite("Nested");
        checkSuite(nested, "Nested", 2, 0, 0);
        checkPassCase(nested, "test1");
        checkPassCase(nested, "test2");
    }

    public void testDuplicatesResolutionOff()
    {
        TestSuiteResult tests = runProcessor("duplicates");
        TestSuiteResult suite = tests.getSuite("com.zutubi.pulse.junit.SimpleTest");
        assertEquals(1, suite.getCases().size());
    }

    public void testDuplicatesResolutionAppend() throws FileLoadException
    {
        pp.setResolveConflicts(TestSuiteResult.Resolution.APPEND.toString());
        TestSuiteResult tests = runProcessor("duplicates");
        TestSuiteResult suite = tests.getSuite("com.zutubi.pulse.junit.SimpleTest");
        assertEquals(3, suite.getCases().size());
        assertNotNull(suite.getCase("testSimple"));
        assertNotNull(suite.getCase("testSimple2"));
        assertNotNull(suite.getCase("testSimple3"));
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
        CommandContext context = new CommandContext(null, outputDir, testResults);
        pp.process(artifact, result, context);
    }
}
