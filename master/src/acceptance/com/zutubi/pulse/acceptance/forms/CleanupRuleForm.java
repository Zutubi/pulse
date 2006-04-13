/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CleanupRuleForm extends BaseForm
{
    public static final String WORK_DIR_ONLY = "workDirOnly";
    public static final String STATE_NAMES = "stateNames";
    public static final String LIMIT = "limit";
    public static final String BUILD_UNITS = "buildUnits";
    private String name;

    public CleanupRuleForm(WebTester tester, String name)
    {
        super(tester);
        this.name = name;
    }

    public String getFormName()
    {
        return name;
    }

    public String[] getFieldNames()
    {
        return new String[]{
                WORK_DIR_ONLY,
                STATE_NAMES,
                LIMIT,
                BUILD_UNITS };
    }

    public int[] getFieldTypes()
    {
        return new int[]{SELECT, SELECT, TEXTFIELD, RADIOBOX};
    }
}
