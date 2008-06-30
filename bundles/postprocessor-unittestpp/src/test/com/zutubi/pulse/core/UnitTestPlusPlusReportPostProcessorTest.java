package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLReportPostProcessorTestBase;

import java.io.File;
import java.net.URL;

/**
 */
public class UnitTestPlusPlusReportPostProcessorTest extends XMLReportPostProcessorTestBase
{
    public UnitTestPlusPlusReportPostProcessorTest()
    {
        super(new UnitTestPlusPlusReportPostProcessor());
    }

    protected File getOutputDir()
    {
        URL resource = getClass().getResource("UnitTestPlusPlusReportPostProcessorTest.basic.xml");
        return new File(resource.getPath()).getParentFile();
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
