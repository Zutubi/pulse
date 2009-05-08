package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * Action allowing a user to clear responsibility for a build.
 */
public class ClearResponsibilityAction extends ResponsibilityActionBase
{
    @Override
    public SimpleResult doExecute()
    {
        buildManager.clearResponsibility(getBuildResult());
        return new SimpleResult(true, null);
    }
}