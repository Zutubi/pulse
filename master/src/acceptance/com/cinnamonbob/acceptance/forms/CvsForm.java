package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public abstract class CvsForm extends BaseForm
{
    public CvsForm(WebTester tester)
    {
        super(tester);
    }

    public String[] getFieldNames()
    {
        return new String[]{"cvs.root", "cvs.module", "cvs.password", "cvs.branch", "minutes", "seconds"};
    }

    public static class Create extends CvsForm
    {
        public Create(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "cvs.setup";
        }
    }

    public static class Edit extends CvsForm
    {
        public Edit(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "cvs.edit";
        }
    }
}