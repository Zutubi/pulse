package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorTestBase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class CUnitReportPostProcessorTest extends XMLTestReportPostProcessorTestBase
{
    public CUnitReportPostProcessorTest()
    {
        super(new CUnitReportPostProcessor(new CUnitReportPostProcessorConfiguration()));
    }

    protected File getOutputDir() throws URISyntaxException
    {
        URL resource = getClass().getResource("CUnitReportPostProcessorTest.basic.xml");
        return new File(resource.toURI()).getParentFile();
    }

    public void testBasic() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("basic");
        assertEquals(1, tests.getSuites().size());
        assertFirstSuite(tests.getSuites().get(0));
    }

    public void testMulti() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("multi");
        assertEquals(3, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
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

    public void testRandomJunkIgnored() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("testRandomJunkIgnored");
        assertEquals(3, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
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

    private void assertFirstSuite(PersistentTestSuiteResult suite)
    {
        checkSuite(suite, "First Suite", 2, 1, 0);
        checkPassCase(suite, "Test Pass");
        checkFailureCase(suite, "Test Fail", "c:\\users\\jsankey\\documents\\visual studio 2008\\projects\\cunitplay\\firsttest\\main.cpp: 35: CU_ASSERT_STRING_EQUAL(gFoo,\"no soup for you!\")");
    }
}
