package com.zutubi.pulse.core.postprocessors.boostregression;

import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorTestBase;
import com.zutubi.pulse.core.postprocessors.api.TestResult;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.FAILURE;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.PASS;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class BoostRegressionPostProcessorTest extends XMLTestReportPostProcessorTestBase
{
    public BoostRegressionPostProcessorTest()
    {
        super(new BoostRegressionPostProcessor(new BoostRegressionPostProcessorConfiguration()));
    }

    protected File getOutputDir() throws URISyntaxException
    {
        URL resource = getClass().getResource("BoostRegressionPostProcessorTest.run.xml");
        return new File(resource.toURI()).getParentFile();
    }

    public void testBasic() throws Exception
    {
        singleLogHelper("compilefail", "iterator", "interoperable_fail", PASS, null);
    }

    public void testRun() throws Exception
    {
        singleLogHelper("run", "statechart", "InvalidResultCopyTestRelaxed", PASS, null);
    }

    public void testRandomJunkIgnored() throws Exception
    {
        singleLogHelper("testRandomJunkIgnored", "statechart", "InvalidResultCopyTestRelaxed", PASS, null);
    }

    public void testBroken() throws Exception
    {
        singleLogHelper("broken", "statechart", "InvalidResultCopyTestRelaxed", FAILURE, "============================[ compile output below ]============================\n" +
                "    compiler error here\n" +
                "============================[ compile output above ]============================\n");
    }

    public void testNested() throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor("nested");
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("algorithm", suite.getName());
        assertEquals(1, suite.getTotal());
        suite = suite.getSuite("minmax");
        assertNotNull(suite);
        checkCase(suite.getCase("minmax"), "minmax", PASS, TestResult.DURATION_UNKNOWN, null);
    }

    private void singleLogHelper(String testName, String suiteName, String caseName, TestStatus status, String message) throws Exception
    {
        PersistentTestSuiteResult tests = runProcessor(testName);
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals(suiteName, suite.getName());
        assertEquals(1, suite.getTotal());
        checkCase(suite.getCase(caseName), caseName, status, TestResult.DURATION_UNKNOWN, message);
    }

}
