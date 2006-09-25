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
        checkSuite(suite, "AnotherTest", 1, 0, 0);

        List<TestCaseResult> children = suite.getCases();
        checkPassCase(children.get(0), "testIt");

        suite = tests.getSuites().get(1);
        checkSuite(suite, "Test", 6, 2, 1);

        children = suite.getCases();
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

}
