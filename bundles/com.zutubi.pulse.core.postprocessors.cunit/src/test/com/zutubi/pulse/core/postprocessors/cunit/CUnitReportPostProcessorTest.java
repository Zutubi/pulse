package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.postprocessors.api.*;

import java.io.IOException;

public class CUnitReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private CUnitReportPostProcessor pp = new CUnitReportPostProcessor(new CUnitReportPostProcessorConfiguration());

    public void testBasic() throws Exception
    {
        TestSuiteResult tests = runProcessor("basic");
        assertEquals(1, tests.getSuites().size());
        assertFirstSuite(tests.getSuites().get(0));
    }

    public void testMulti() throws Exception
    {
        TestSuiteResult tests = runProcessor("multi");
        checkMultipleSuites(tests);
    }

    public void testRandomJunkIgnored() throws Exception
    {
        TestSuiteResult tests = runProcessor("testRandomJunkIgnored");
        checkMultipleSuites(tests);
    }

    private void checkMultipleSuites(TestSuiteResult tests)
    {
        assertEquals(3, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(1);
        checkStatusCounts(suite, "Borked Suite", 1, 0, 1, 0, 0);
        assertEquals(new TestCaseResult("Suite Failure Notification", TestResult.DURATION_UNKNOWN, TestStatus.ERROR, "Suite Initialization Failed"), suite.getCases().iterator().next());

        assertFirstSuite(tests.getSuites().get(0));

        suite = tests.getSuites().get(2);
        checkStatusCounts(suite, "Last Suite", 4, 3, 0, 0, 0);

        assertEquals(new TestCaseResult("Test Two Pass", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), suite.findCase("Test Two Pass"));
        assertEquals(new TestCaseResult("Test Two Fail", TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 46: CU_ASSERT_FALSE(1)\n" +
                "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 47: CU_ASSERT_TRUE(0)"), suite.findCase("Test Two Fail"));
        assertEquals(new TestCaseResult("Test Pass Fail", TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 53: CU_ASSERT_TRUE(0)"), suite.findCase("Test Pass Fail"));
        assertEquals(new TestCaseResult("Test Fatal", TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 58: 0"), suite.findCase("Test Fatal"));
    }

    private void assertFirstSuite(TestSuiteResult suite)
    {
        checkStatusCounts(suite, "First Suite", 2, 1, 0, 0, 0);
        assertEquals(new TestCaseResult("Test Pass", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), suite.findCase("Test Pass"));
        assertEquals(new TestCaseResult("Test Fail", TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 35: CU_ASSERT_STRING_EQUAL(gFoo,\"no soup for you!\")"), suite.findCase("Test Fail"));
    }

    private TestSuiteResult runProcessor(String name) throws IOException
    {
        return runProcessorAndGetTests(pp, name, EXTENSION_XML);
    }
}
