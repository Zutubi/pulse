/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.List;

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
        StoredFileArtifact artifact = runProcessor("simple");

        List<TestResult> tests = artifact.getTests();
        assertEquals(3, tests.size());
        checkWarning(tests.get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");
        checkWarning(tests.get(1), "com.zutubi.pulse.junit.NonConstructableTest", 100, "no public constructor");

        TestResult result = artifact.getTests().get(2);
        assertTrue(result instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) result;
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        List<TestResult> children = suite.getChildren();
        assertEquals(3, children.size());
        checkCase((TestCaseResult) children.get(0), "testSimple", TestCaseResult.Status.PASS, 0, null);
        checkCase((TestCaseResult) children.get(1), "testAssertionFailure", TestCaseResult.Status.FAILURE, 10,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)");
        checkCase((TestCaseResult) children.get(2), "testThrowException", TestCaseResult.Status.ERROR, 10,
                "java.lang.RuntimeException: random message\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)");
    }

    public void testSingle()
    {
        StoredFileArtifact artifact = runProcessor("single");

        List<TestResult> tests = artifact.getTests();
        assertEquals(1, tests.size());

        TestResult result = artifact.getTests().get(0);
        assertTrue(result instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) result;
        assertEquals("com.zutubi.pulse.core.JUnitReportPostProcessorTest", suite.getName());
        assertEquals(391, suite.getDuration());

        List<TestResult> children = suite.getChildren();
        assertEquals(3, children.size());
        checkCase((TestCaseResult) children.get(0), "testSimple", TestCaseResult.Status.PASS, 291, null);
        checkCase((TestCaseResult) children.get(1), "testFailure", TestCaseResult.Status.FAILURE, 10,
                "junit.framework.AssertionFailedError\n" +
                        "\tat com.zutubi.pulse.core.JUnitReportPostProcessorTest.testFailure(JUnitReportPostProcessorTest.java:63)");
        checkCase((TestCaseResult) children.get(2), "testError", TestCaseResult.Status.ERROR, 0,
                "java.lang.RuntimeException: whoops!\n" +
                        "\tat com.zutubi.pulse.core.JUnitReportPostProcessorTest.testError(JUnitReportPostProcessorTest.java:68)");
    }

    private void checkWarning(TestResult testResult, String name, long duration, String contents)
    {
        assertTrue(testResult instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) testResult;
        assertEquals(name, suite.getName());
        assertEquals(duration, suite.getDuration());

        List<TestResult> children = suite.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof TestCaseResult);
        TestCaseResult caseResult = (TestCaseResult) children.get(0);
        assertEquals("warning", caseResult.getName());
        assertEquals(10, caseResult.getDuration());
        assertTrue(caseResult.getMessage().contains(contents));
    }
}
