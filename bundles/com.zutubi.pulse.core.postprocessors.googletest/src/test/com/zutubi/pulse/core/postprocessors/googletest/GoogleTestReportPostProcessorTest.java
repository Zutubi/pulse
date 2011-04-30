package com.zutubi.pulse.core.postprocessors.googletest;

import com.zutubi.pulse.core.postprocessors.api.*;

import java.io.IOException;

import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;

public class GoogleTestReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private GoogleTestReportPostProcessor pp = new GoogleTestReportPostProcessor(new GoogleTestReportPostProcessorConfiguration());

    public void testBasic() throws Exception
    {
        TestPostProcessorContext context = runProcessor(pp);
        TestSuiteResult tests = context.getTestSuiteResult();

        assertEquals(2, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");

        TestSuiteResult suite = tests.getSuites().get(1);
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(3, children.length);
        assertEquals(new TestCaseResult("testSimple", 0, PASS), children[0]);
        assertEquals(new TestCaseResult("testAssertionFailure", 10, FAILURE,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                        "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)"),
                children[1]);
        assertEquals(new TestCaseResult("testThrowException", 10, ERROR,
                "java.lang.RuntimeException: random message\n" +
                        "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)"),
                children[2]);
    }

    public void testItself() throws IOException
    {
        TestPostProcessorContext context = runProcessor(pp);
        TestSuiteResult tests = context.getTestSuiteResult();
        assertEquals(419, tests.getTotal());
        assertEquals(13, tests.getTotalWithStatus(SKIPPED));

        TestSuiteResult suite = tests.findSuite("DisabledTest");
        assertNotNull(suite);

        TestCaseResult disabledCase = suite.findCase("TestShouldNotRun");
        assertNotNull(disabledCase);
        assertEquals(TestStatus.SKIPPED, disabledCase.getStatus());

        TestCaseResult notDisabledCase = suite.findCase("NotDISABLED_TestShouldRun");
        assertNotNull(notDisabledCase);
        assertEquals(TestStatus.PASS, notDisabledCase.getStatus());
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
}
