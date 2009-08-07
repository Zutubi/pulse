package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.StringUtils;

import java.io.File;

/**
 * <p>
 * A support base for post processors that find test results.  Includes
 * standard support for failing the build on a failed test, and for adding
 * to a custom child suite.
 * </p>
 * <p>
 * Note that it is common for reports to be in XML format, for which a more
 * specific support class ({@link XMLTestReportPostProcessorSupport}) exists.
 * </p>
 *
 * @see XMLTestReportPostProcessorSupport
 */
public abstract class TestReportPostProcessorSupport extends PostProcessorSupport
{
    protected TestReportPostProcessorSupport(TestReportPostProcessorConfigurationSupport config)
    {
        super(config);
    }

    @Override
    public TestReportPostProcessorConfigurationSupport getConfig()
    {
        return (TestReportPostProcessorConfigurationSupport) super.getConfig();
    }

    public void process(File artifactFile, PostProcessorContext ppContext)
    {
        if(artifactFile.isFile())
        {
            TestReportPostProcessorConfigurationSupport config = getConfig();
            TestSuiteResult suiteResult = new TestSuiteResult(null);
            TestSuiteResult accumulateSuite = suiteResult;
            String suite = config.getSuite();
            if (StringUtils.stringSet(suite))
            {
                accumulateSuite = new TestSuiteResult(suite);
                suiteResult.addSuite(accumulateSuite);
            }

            extractTestResults(artifactFile, ppContext, accumulateSuite);
            ppContext.addTests(suiteResult, config.getResolveConflicts());

            ResultState state = ppContext.getResultState();
            if (config.getFailOnFailure() && state != ResultState.ERROR && state != ResultState.FAILURE)
            {
                if (containsBrokenCase(suiteResult))
                {
                    ppContext.failCommand("One or more test cases failed.");
                }
            }
        }
    }

    private boolean containsBrokenCase(TestSuiteResult suite)
    {
        for (TestCaseResult caseResult: suite.getCases())
        {
            if (caseResult.getStatus().isBroken())
            {
                return true;
            }
        }

        for (TestSuiteResult nestedSuite: suite.getSuites())
        {
            if (containsBrokenCase(nestedSuite))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Called once for each file to post process for test results.  Test
     * results found should be added to the given suite.  Nested suites are
     * supported.  Additional manipulation of the artifact being processed or
     * corresponding command result can be done via the context.
     *
     * @param file      file to post process to find test results
     * @param ppContext context in which the post processor is executing
     * @param tests     root test suite to add discovered test results to (note
     *                  this suite itself is unnamed and not added to the
     *                  recipe, rather it is just used to collect suites and
     *                  cases which are then extracted and added directly to
     *                  the recipe test results)
     */
    protected abstract void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests);
}
