package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CleanupPolicyForm extends BaseForm
{
    public CleanupPolicyForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "cleanup.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"enableWorkDirCleanup", "policy.workDirDays",
                "enableResultCleanup", "policy.resultDays"};
    }
}
