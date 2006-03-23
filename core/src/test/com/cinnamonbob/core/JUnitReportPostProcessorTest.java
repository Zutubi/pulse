package com.cinnamonbob.core;

import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.util.List;

/**
 */
public class JUnitReportPostProcessorTest extends BobTestCase
{
    private JUnitReportPostProcessor pp;

    public void setUp() throws Exception
    {
        super.setUp();
        pp = new JUnitReportPostProcessor();
    }

    public void tearDown() throws Exception
    {
        pp = null;
        super.tearDown();
    }

    public void testSimple()
    {
        File root = getBobRoot();
        File outputDir = new File(root, FileSystemUtils.composeFilename("core", "src", "test", "com", "cinnamonbob", "core"));

        StoredFileArtifact artifact = new StoredFileArtifact(getClass().getSimpleName() + ".simple.xml");
        pp.process(outputDir, artifact, new CommandResult("test"));

        List<TestResult> tests = artifact.getTests();
        assertEquals(3, tests.size());
        checkWarning(tests.get(0), "com.cinnamonbob.junit.EmptyTest", 91, "No tests found");
        checkWarning(tests.get(1), "com.cinnamonbob.junit.NonConstructableTest", 100, "no public constructor");

        TestResult result = artifact.getTests().get(2);
        assertTrue(result instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) result;
        assertEquals("com.cinnamonbob.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        List<TestResult> children = suite.getChildren();
        assertEquals(3, children.size());
        checkCase((TestCaseResult) children.get(0), "testSimple", TestCaseResult.Status.PASS, 0, null);
        checkCase((TestCaseResult) children.get(1), "testAssertionFailure", TestCaseResult.Status.FAILURE, 10,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                "\tat com.cinnamonbob.junit.SimpleTest.testAssertionFailure(Unknown Source)");
        checkCase((TestCaseResult) children.get(2), "testThrowException", TestCaseResult.Status.ERROR, 10,
                "java.lang.RuntimeException: random message\n" +
                "\tat com.cinnamonbob.junit.SimpleTest.testThrowException(Unknown Source)");
    }

    private void checkCase(TestCaseResult caseResult, String name, TestCaseResult.Status status, long duration, String message)
    {
        assertEquals(name, caseResult.getName());
        assertEquals(status, caseResult.getStatus());
        assertEquals(duration, caseResult.getDuration());
        assertEquals(message, caseResult.getMessage());
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
