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

    public void setFormElements(String... values)
    {
        tester.assertFormPresent(getFormName());
        tester.setWorkingForm(getFormName());
        if (values[0] != null)
            setCheckboxChecked(ENABLE_WORK_DIR_CLEANUP, Boolean.valueOf(values[0]));
        if (values[1] != null)
            tester.setFormElement(getFieldNames()[1], values[1]);
        if (values[2] != null)
            setCheckboxChecked(ENABLE_RESULT_CLEANUP, Boolean.valueOf(values[2]));
        if (values[3] != null)
            tester.setFormElement(getFieldNames()[3], values[3]);
    }

    public void assertFormElements(String... values)
    {
        tester.assertFormPresent(getFormName());
        tester.setWorkingForm(getFormName());

        assertCheckboxChecked(getFieldNames()[0], Boolean.valueOf(values[0]));
        tester.assertFormElementEquals(getFieldNames()[1], values[1]);
        assertCheckboxChecked(getFieldNames()[2], Boolean.valueOf(values[2]));
        tester.assertFormElementEquals(getFieldNames()[3], values[3]);
    }

}
