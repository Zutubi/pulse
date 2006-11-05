package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureDashboardForm extends BaseForm
{
    public ConfigureDashboardForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "dashboard.configuration";
    }

    public String[] getFieldNames()
    {
        return new String[] { "myBuildsCount", "buildCount", "showAllProjects", "projects", "shownGroups", "showMyChanges", "user.myChangesCount", "showProjectChanges", "user.projectChangesCount" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX, MULTI_SELECT, MULTI_SELECT, CHECKBOX, TEXTFIELD, CHECKBOX, TEXTFIELD };
    }
}
