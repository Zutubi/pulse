package com.zutubi.pulse.model;

/**
 * <class-comment/>
 */
public class FailedAndFirstSuccessNotifyCondition implements NotifyCondition
{
    private SuccessAfterFailureNotifyCondition successAfterFailure;
    private FailedNotifyCondition failure;

    public FailedAndFirstSuccessNotifyCondition()
    {
        successAfterFailure = new SuccessAfterFailureNotifyCondition();
        failure = new FailedNotifyCondition();
    }

    public boolean satisfied(BuildResult result)
    {
        return failure.satisfied(result) || successAfterFailure.satisfied(result);
    }

    // this is rather awkward...
    public void setBuildManager(BuildManager buildManager)
    {
        successAfterFailure.setBuildManager(buildManager);
    }
}
