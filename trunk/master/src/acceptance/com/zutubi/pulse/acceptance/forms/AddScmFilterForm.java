package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class AddScmFilterForm extends BaseForm
{
    public AddScmFilterForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "scm.filter.excluded.add";
    }

    public String[] getFieldNames()
    {
        return new String[]{"excludedPath"};
    }

    public void addFormElements(String... args)
    {
        setFormElements(args);
        tester.clickLink("add");
    }
}
