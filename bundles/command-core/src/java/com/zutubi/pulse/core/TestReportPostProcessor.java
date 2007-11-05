package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.File;

/**
 */
public abstract class TestReportPostProcessor extends SelfReference implements PostProcessor
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

    public void process(StoredFileArtifact artifact, CommandResult result, ExecutionContext context)
    {
        TestSuiteResult testResults = context.getInternalValue(BuildProperties.PROPERTY_TEST_RESULTS, TestSuiteResult.class);
        File outputDir = context.getInternalFile(BuildProperties.PROPERTY_OUTPUT_DIR);
        int brokenBefore = testResults.getSummary().getBroken();

        File file = new File(outputDir, artifact.getPath());
        if(file.isFile())
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

            internalProcess(result, file, parentSuite);

            if(suite != null)
            {
                testResults.add(parentSuite);
            }
            
            if(failOnFailure && !result.failed() && !result.errored())
            {
                int brokenAfter = testResults.getSummary().getBroken();
                if(brokenAfter > brokenBefore)
                {
                    result.failure("One or more test cases failed.");
                }
            }
        }
    }

    protected abstract void internalProcess(CommandResult result, File file, TestSuiteResult suite);
}
