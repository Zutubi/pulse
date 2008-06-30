package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.File;

/**
 */
public abstract class TestReportPostProcessorSupport extends PostProcessorSupport
{
    private String suite;
    private boolean failOnFailure = true;

    public void setSuite(String suite)
    {
        this.suite = suite;
    }

    public boolean getFailOnFailure()
    {
        return failOnFailure;
    }

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

    protected abstract void process(File file, TestSuiteResult suite, PostProcessorContext ppContext);
}
