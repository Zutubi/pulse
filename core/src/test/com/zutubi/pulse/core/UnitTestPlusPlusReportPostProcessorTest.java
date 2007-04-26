package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.IOException;

/**
 */
public class UnitTestPlusPlusReportPostProcessorTest extends XMLReportPostProcessorTestBase
{
    public UnitTestPlusPlusReportPostProcessorTest()
    {
        this(null);
    }

    public UnitTestPlusPlusReportPostProcessorTest(String name)
    {
        super(name, new UnitTestPlusPlusReportPostProcessor());
    }

    public void setUp() throws IOException
    {
        pp = new UnitTestPlusPlusReportPostProcessor();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBasic()
    {
        TestSuiteResult tests = runProcessor("basic");

        assertEquals(3, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("DefaultSuite", suite.getName());
        assertEquals(1, suite.getTotal());
        checkCase(suite.getCase("SuiteLess"), "SuiteLess", TestCaseResult.Status.PASS, 0, null);

        suite = tests.getSuites().get(1);
        assertEquals("SuiteOne", suite.getName());
        assertEquals(3, suite.getTotal());
        checkCase(suite.getCase("TestOne"), "TestOne", TestCaseResult.Status.PASS, 0, null);
        checkCase(suite.getCase("TestTwo"), "TestTwo", TestCaseResult.Status.FAILURE, 1, "utpp.cpp(14) : false");
        checkCase(suite.getCase("TestThrow"), "TestThrow", TestCaseResult.Status.FAILURE, 107, "utpp.cpp(17) : Unhandled exception: Crash!");
        
        suite = tests.getSuites().get(2);
        assertEquals("SuiteTwo", suite.getName());
        assertEquals(1, suite.getTotal());
        checkCase(suite.getCase("TestOne"), "TestOne", TestCaseResult.Status.PASS, 0, null);
    }

}
