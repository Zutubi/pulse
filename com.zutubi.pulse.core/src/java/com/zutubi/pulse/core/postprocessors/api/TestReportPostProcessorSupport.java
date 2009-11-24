package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * A support base for post processors that find test results.  Includes
 * standard support for failing the build on a failed test, and for adding
 * to a custom child suite.
 * </p>
 * <p>
 * Note that it is common for reports to be in XML format, for which more
 * specific support classes ({@link DomTestReportPostProcessorSupport} and
 * {@link StAXTestReportPostProcessorSupport}) exist.
 * </p>
 *
 * @see StAXTestReportPostProcessorSupport
 * @see DomTestReportPostProcessorSupport
 */
public abstract class TestReportPostProcessorSupport extends PostProcessorSupport
{
    private boolean testMode = false;

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
        if (artifactFile.isFile())
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
            processExpectedFailures(accumulateSuite, ppContext);
            ppContext.addTests(suiteResult, config.getResolveConflicts());

            ResultState state = ppContext.getResultState();
            if (config.getFailOnFailure() && state != ResultState.ERROR && state != ResultState.FAILURE)
            {
                if (containsUnexpectedBrokenCase(suiteResult))
                {
                    ppContext.failCommand("One or more test cases failed.");
                }
            }
        }
    }

    private void processExpectedFailures(final TestSuiteResult suite, PostProcessorContext context)
    {
        String failureFileName = getConfig().getExpectedFailureFile();
        if (StringUtils.stringSet(failureFileName))
        {
            File failureFile = new File(failureFileName);
            if (!failureFile.isAbsolute())
            {
                failureFile = new File(context.getExecutionContext().getWorkingDir(), failureFileName);
            }

            if (failureFile.isFile())
            {
                try
                {
                    IOUtils.forEachLine(failureFile, new UnaryProcedure<String>()
                    {
                        public void process(String s)
                        {
                            processExpectedFailure(suite, s);
                        }
                    });
                }
                catch (IOException e)
                {
                    context.addFeature(new Feature(Feature.Level.WARNING, "Unable to read expected failure file '" + failureFile.getAbsolutePath() + "': " + e.getMessage()));
                }
            }
        }
    }

    private void processExpectedFailure(TestSuiteResult suite, String line)
    {
        String[] parts = StringUtils.split(line, '/');
        for (int i = 0; i < parts.length - 1; i++)
        {
            String suiteName = WebUtils.percentDecode(parts[i]);
            suite = suite.findSuite(suiteName);
            if (suite == null)
            {
                return;
            }
        }

        String caseName = WebUtils.percentDecode(parts[parts.length - 1]);
        TestCaseResult caseResult = suite.findCase(caseName);
        if (caseResult != null && caseResult.getStatus().isBroken())
        {
            caseResult.setStatus(TestStatus.EXPECTED_FAILURE);
        }
    }

    private boolean containsUnexpectedBrokenCase(TestSuiteResult suite)
    {
        for (TestCaseResult caseResult: suite.getCases())
        {
            TestStatus status = caseResult.getStatus();
            if (status.isBroken() && status != TestStatus.EXPECTED_FAILURE)
            {
                return true;
            }
        }

        for (TestSuiteResult nestedSuite: suite.getSuites())
        {
            if (containsUnexpectedBrokenCase(nestedSuite))
            {
                return true;
            }
        }

        return false;
    }

    protected void handleException(File file, PostProcessorContext ppContext, Exception e)
    {
        String message = e.getClass().getName() + " processing report '" + file.getAbsolutePath() + "'";
        handleException(message, ppContext, e);
    }

    protected void handleException(String message, PostProcessorContext ppContext, Exception e)
    {
        if (testMode)
        {
            throw new RuntimeException(e);
        }
        else
        {
            if(e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }
            ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, message));
        }
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

    /**
     * If called, exceptions during processing will be propagated instead of
     * handled and recorded as warnings.  This is intended for unit tests of
     * the processor itself.
     */
    public void enableTestMode()
    {
        testMode = true;
    }
}
