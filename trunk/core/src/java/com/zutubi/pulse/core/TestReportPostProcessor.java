package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;

/**
 */
public abstract class TestReportPostProcessor implements PostProcessor
{
    private String name;
    private boolean failOnFailure = true;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setFailOnFailure(boolean failOnFailure)
    {
        this.failOnFailure = failOnFailure;
    }

    public void process(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        int brokenBefore = context.getTestResults().getSummary().getBroken();

        internalProcess(artifact, result, context);

        if(failOnFailure && !result.failed() && !result.errored())
        {
            int brokenAfter = context.getTestResults().getSummary().getBroken();
            if(brokenAfter > brokenBefore)
            {
                result.failure("One or more test cases failed.");
            }
        }
    }

    protected abstract void internalProcess(StoredFileArtifact artifact, CommandResult result, CommandContext context);

    public Object getValue()
    {
        return this;
    }

}
