package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.TestSuiteResult;

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
    /** @see #setSuite(String)  */
    private String suite;
    /** @see #setFailOnFailure(boolean) */
    private boolean failOnFailure = true;

    /**
     * Sets the name of a nested suite to add all found test results to.
     * This is especially useful when the same tests are processed twice in a
     * build.  By using two processors with different nested suites
     * collisions may be avoided.
     *
     * @param suite name of a nested suite to create and add all tests to
     */
    public void setSuite(String suite)
    {
        this.suite = suite;
    }

    /**
     * @see #setFailOnFailure(boolean)
     * @return
     */
    public boolean getFailOnFailure()
    {
        return failOnFailure;
    }

    /**
     * If set to true, the command (and thus build) will be failed if any
     * failed test case is discovered by this processor.  This flag is true
     * by default.
     *
     * @param failOnFailure true to fail build on a failed test case
     */
    public void setFailOnFailure(boolean failOnFailure)
    {
        this.failOnFailure = failOnFailure;
    }

    protected void process(File artifactFile, PostProcessorContext ppContext)
    {
        TestSuiteResult testResults = ppContext.getTestSuite();
        int brokenBefore = testResults.getSummary().getBroken();

        if(artifactFile.isFile())
        {
            TestSuiteResult parentSuite;
            if(suite == null)
            {
                parentSuite = testResults;
            }
            else
            {
                parentSuite = new TestSuiteResult(suite);
            }

            process(artifactFile, parentSuite, ppContext);

            if(suite != null)
            {
                testResults.add(parentSuite);
            }

            ResultState state = ppContext.getResultState();
            if(failOnFailure && state != ResultState.ERROR && state != ResultState.FAILURE)
            {
                int brokenAfter = testResults.getSummary().getBroken();
                if(brokenAfter > brokenBefore)
                {
                    ppContext.failCommand("One or more test cases failed.");
                }
            }
        }
    }

    /**
     * Called once for each file to post process for test results.  Test
     * results found should be added to the given suite.  Nested suites are
     * supported.  Additional manipulation of the artifact being processed or
     * corresponding command result can be done via the context.
     *
     * @param file      file to post process to find test results
     * @param suite     root test suite to add discovered test results to
     * @param ppContext context in which the post processor is executing
     */
    protected abstract void process(File file, TestSuiteResult suite, PostProcessorContext ppContext);
}
