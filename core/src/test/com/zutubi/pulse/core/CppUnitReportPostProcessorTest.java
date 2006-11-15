package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;

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
        TestSuiteResult tests = runProcessor("basic");

        assertEquals(2, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }

    public void testTwoReports()
    {
        TestSuiteResult tests = runProcessor("basic", "second");

        assertEquals(3, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertHelloWorld(suite, "Second");

        suite = tests.getSuites().get(2);
        assertTest(suite, "Test");
    }

    private void assertHelloWorld(TestSuiteResult suite, String name)
    {
        checkSuite(suite, name, 1, 0, 0);

        List<TestCaseResult> children = suite.getCases();
        checkPassCase(children.get(0), "testHelloWorld");
    }

    private void assertTest(TestSuiteResult suite, String name)
    {
        checkSuite(suite, name, 6, 2, 1);

        List<TestCaseResult> children = suite.getCases();
        checkFailureCase(children.get(0), "testFailure", "At file cppunit.cpp line 34\n" +
                                                         "assertion failed\n" +
                                                         "- Expression: 1 == 2");
        checkErrorCase(children.get(1), "testThrow", "uncaught exception of type std::exception\n" +
                                                     "- St9exception");
        checkFailureCase(children.get(2), "testDidntThrow", "expected exception not thrown\n" +
                                                            "- Expected exception type: std::exception");
        checkPassCase(children.get(3), "testHelloWorld");
        checkPassCase(children.get(4), "testExpectedThrow");
        checkPassCase(children.get(5), "testExpectedFailure");
    }

    private void assertAnotherTest(TestSuiteResult suite, String name)
    {
        checkSuite(suite, name, 1, 0, 0);

        List<TestCaseResult> children = suite.getCases();
        checkPassCase(children.get(0), "testIt");
    }
}
