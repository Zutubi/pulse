package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CleanupPolicyForm extends BaseForm
{
    public static final String ENABLE_WORK_DIR_CLEANUP = "enableWorkDirCleanup";
    public static final String ENABLE_RESULT_CLEANUP = "enableResultCleanup";

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
        return new String[]{ENABLE_WORK_DIR_CLEANUP, "policy.workDirDays",
                ENABLE_RESULT_CLEANUP, "policy.resultDays"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{CHECKBOX, TEXTFIELD, CHECKBOX, TEXTFIELD};
    }
}
