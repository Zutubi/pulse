package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CvsEditForm extends BaseForm
{
    public CvsEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "cvs.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"cvs.root", "cvs.module", "cvs.password", "cvs.path"};
    }
}
