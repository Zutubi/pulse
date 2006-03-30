package com.cinnamonbob.model;

/**
 * This notify condition triggers when the received build result is the first
 * successful build after a one or more failures.
 * 
 */
public class SuccessAfterFailureNotifyCondition implements NotifyCondition
{
    private BuildManager buildManager;

    public SuccessAfterFailureNotifyCondition()
    {
    }

    /**
     * The system build manager is required to lookup previous build results.
     *
     * @param buildManager
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public boolean satisfied(BuildResult result)
    {
        if (!result.succeeded())
        {
            return false;
        }

        // retrieve the previous result. If it was a failure, then this condition is satisfied.
        BuildResult previous = buildManager.getPreviousBuildResult(result);
        return !previous.succeeded();
    }
}
