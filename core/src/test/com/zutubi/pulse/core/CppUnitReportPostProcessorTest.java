/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;

import java.io.IOException;
import java.util.List;

/**
 */
public class CppUnitReportPostProcessorTest extends XMLReportPostProcessorTestBase
{
    public CppUnitReportPostProcessorTest()
    {
        this(null);
    }

    public CppUnitReportPostProcessorTest(String name)
    {
        super(name, new CppUnitReportPostProcessor());
    }

    public void setUp() throws IOException
    {
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBasic()
    {
        StoredFileArtifact artifact = runProcessor("basic");

        List<TestResult> tests = artifact.getTests();
        assertEquals(2, tests.size());

        TestSuiteResult suite = (TestSuiteResult) tests.get(0);
        checkSuite(suite, "AnotherTest", 1, 0, 0);

        List<TestResult> children = suite.getChildren();
        checkPassCase((TestCaseResult) children.get(0), "testIt");

        suite = (TestSuiteResult) tests.get(1);
        checkSuite(suite, "Test", 6, 2, 1);

        children = suite.getChildren();
        checkFailureCase((TestCaseResult) children.get(0), "testFailure", "At file cppunit.cpp line 34\n" +
                "assertion failed\n" +
                "- Expression: 1 == 2");
        checkErrorCase((TestCaseResult) children.get(1), "testThrow", "uncaught exception of type std::exception\n" +
                "- St9exception");
        checkFailureCase((TestCaseResult) children.get(2), "testDidntThrow", "expected exception not thrown\n" +
                "- Expected exception type: std::exception");
        checkPassCase((TestCaseResult) children.get(3), "testHelloWorld");
        checkPassCase((TestCaseResult) children.get(4), "testExpectedThrow");
        checkPassCase((TestCaseResult) children.get(5), "testExpectedFailure");
    }

}
