package com.zutubi.pulse.core.postprocessors.unittestpp;

import com.zutubi.pulse.core.model.PersistentTestCaseResult;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorTestBase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 */
public class UnitTestPlusPlusReportPostProcessorTest extends XMLTestReportPostProcessorTestBase
{
    public UnitTestPlusPlusReportPostProcessorTest()
    {
        super(new UnitTestPlusPlusReportPostProcessor());
    }

    protected File getOutputDir() throws URISyntaxException
    {
        URL resource = getClass().getResource("UnitTestPlusPlusReportPostProcessorTest.basic.xml");
        return new File(resource.toURI()).getParentFile();
    }

    public void testBasic() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("basic");

        assertEquals(3, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("DefaultSuite", suite.getName());
        assertEquals(1, suite.getTotal());
        checkCase(suite.getCase("SuiteLess"), "SuiteLess", PersistentTestCaseResult.Status.PASS, 0, null);

        suite = tests.getSuites().get(1);
        assertEquals("SuiteOne", suite.getName());
        assertEquals(3, suite.getTotal());
        checkCase(suite.getCase("TestOne"), "TestOne", PersistentTestCaseResult.Status.PASS, 0, null);
        checkCase(suite.getCase("TestTwo"), "TestTwo", PersistentTestCaseResult.Status.FAILURE, 1, "utpp.cpp(14) : false");
        checkCase(suite.getCase("TestThrow"), "TestThrow", PersistentTestCaseResult.Status.FAILURE, 107, "utpp.cpp(17) : Unhandled exception: Crash!");
        
        suite = tests.getSuites().get(2);
        assertEquals("SuiteTwo", suite.getName());
        assertEquals(1, suite.getTotal());
        checkCase(suite.getCase("TestOne"), "TestOne", PersistentTestCaseResult.Status.PASS, 0, null);
    }

}
