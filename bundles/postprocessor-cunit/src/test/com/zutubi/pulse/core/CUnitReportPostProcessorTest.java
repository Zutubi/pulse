package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorTestBase;

import java.io.File;
import java.net.URL;

/**
 */
public class CUnitReportPostProcessorTest extends XMLTestReportPostProcessorTestBase
{
    public CUnitReportPostProcessorTest()
    {
        super(new CUnitReportPostProcessor());
    }

    protected File getOutputDir()
    {
        URL resource = getClass().getResource("CUnitReportPostProcessorTest.basic.xml");
        return new File(resource.getPath()).getParentFile();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBasic()
    {
        TestSuiteResult tests = runProcessor("basic");
        assertEquals(1, tests.getSuites().size());
        assertFirstSuite(tests.getSuites().get(0));
    }

    public void testMulti()
    {
        TestSuiteResult tests = runProcessor("multi");
        assertEquals(3, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        checkSuite(suite, "Borked Suite", 1, 0, 1);
        checkErrorCase(suite.getCases().iterator().next(), "Suite Failure Notification", "Suite Initialization Failed");

        assertFirstSuite(tests.getSuites().get(1));

        suite = tests.getSuites().get(2);
        checkSuite(suite, "Last Suite", 4, 3, 0);

        checkPassCase(suite, "Test Two Pass");
        checkFailureCase(suite, "Test Two Fail", "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 46: CU_ASSERT_FALSE(1)\n" +
                "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 47: CU_ASSERT_TRUE(0)");
        checkFailureCase(suite, "Test Pass Fail", "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 53: CU_ASSERT_TRUE(0)");
        checkFailureCase(suite, "Test Fatal", "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 58: 0");
    }

    private void assertFirstSuite(TestSuiteResult suite)
    {
        checkSuite(suite, "First Suite", 2, 1, 0);
        checkPassCase(suite, "Test Pass");
        checkFailureCase(suite, "Test Fail", "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 35: CU_ASSERT_STRING_EQUAL(gFoo,\"no soup for you!\")");
    }
}
