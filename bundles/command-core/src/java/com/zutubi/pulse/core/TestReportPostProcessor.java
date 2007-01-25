package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.TestSuiteResult;

import java.io.File;

/**
 */
public abstract class TestReportPostProcessor implements PostProcessor
{
    private String name;
    private String suite;
    private boolean failOnFailure = true;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

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

    public void process(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        int brokenBefore = context.getTestResults().getSummary().getBroken();

        File file = new File(context.getOutputDir(), artifact.getPath());
        if(file.isFile())
        {
            TestSuiteResult parentSuite;
            if(suite == null)
            {
                parentSuite = context.getTestResults();
            }
            else
            {
                parentSuite = new TestSuiteResult(suite);
            }

            internalProcess(result, file, parentSuite);

            if(suite != null)
            {
                context.getTestResults().add(parentSuite);
            }
            
            if(failOnFailure && !result.failed() && !result.errored())
            {
                int brokenAfter = context.getTestResults().getSummary().getBroken();
                if(brokenAfter > brokenBefore)
                {
                    result.failure("One or more test cases failed.");
                }
            }
        }
    }

    protected abstract void internalProcess(CommandResult result, File file, TestSuiteResult suite);

    public Object getValue()
    {
        return this;
    }

}
