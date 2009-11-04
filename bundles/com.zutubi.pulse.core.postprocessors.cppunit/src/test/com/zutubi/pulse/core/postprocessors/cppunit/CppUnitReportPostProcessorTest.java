package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.model.PersistentTestCaseResult;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorTestBase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 */
public class CppUnitReportPostProcessorTest extends XMLTestReportPostProcessorTestBase
{
    public CppUnitReportPostProcessorTest()
    {
        super(new CppUnitReportPostProcessor(new CppUnitReportPostProcessorConfiguration()));
    }

    protected File getOutputDir() throws URISyntaxException
    {
        URL resource = getClass().getResource("CppUnitReportPostProcessorTest.basic.xml");
        return new File(resource.toURI()).getParentFile();
    }

    public void testBasic() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("basic");

        assertEquals(2, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }

    public void testTwoReports() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("basic", "second");

        assertEquals(3, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertHelloWorld(suite, "Second");

        suite = tests.getSuites().get(2);
        assertTest(suite, "Test");
    }

    public void testEmptyTags() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("emptytags");

        assertEquals(1, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals(1, suite.getCases().size());

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        PersistentTestCaseResult caseResult = children[0];
        assertEquals("all", caseResult.getName());
        assertEquals(1, caseResult.getErrors());
    }

    public void testParentSuite() throws Exception
    {
        pp.getConfig().setSuite("parent");
        PersistentTestSuiteResult tests = runProcessor("basic");

        List<PersistentTestSuiteResult> topLevelSuites = tests.getSuites();
        assertEquals(1, topLevelSuites.size());
        assertEquals("parent", topLevelSuites.get(0).getName());
        
        tests = topLevelSuites.get(0);
        assertEquals(2, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }

    public void testRandomJunkIgnored() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("testRandomJunkIgnored");

        assertEquals(2, tests.getSuites().size());

        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }
    
    private void assertHelloWorld(PersistentTestSuiteResult suite, String name)
    {
        checkSuite(suite, name, 1, 0, 0);

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        checkPassCase(children[0], "testHelloWorld");
    }

    private void assertTest(PersistentTestSuiteResult suite, String name)
    {
        checkSuite(suite, name, 6, 2, 1);

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        checkFailureCase(children[0], "testFailure", "At file cppunit.cpp line 34\n" +
                                                         "assertion failed\n" +
                                                         "- Expression: 1 == 2");
        checkErrorCase(children[1], "testThrow", "uncaught exception of type std::exception\n" +
                                                     "- St9exception");
        checkFailureCase(children[2], "testDidntThrow", "expected exception not thrown\n" +
                                                            "- Expected exception type: std::exception");
        checkPassCase(children[3], "testHelloWorld");
        checkPassCase(children[4], "testExpectedThrow");
        checkPassCase(children[5], "testExpectedFailure");
    }

    private void assertAnotherTest(PersistentTestSuiteResult suite, String name)
    {
        checkSuite(suite, name, 1, 0, 0);

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        checkPassCase(children[0], "testIt");
    }
}
