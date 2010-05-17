package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * Action allowing a user to clear responsibility for a project.
 */
public class ClearResponsibilityAction extends ResponsibilityActionBase
{
    @Override
    public SimpleResult doExecute()
    {
        projectManager.clearResponsibility(getProject());
        return new TrivialSuccessfulResult();
    }
}