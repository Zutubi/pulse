package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The CVS scm forms. Both create and edit have common field names,
 * and therefore have been layed out in this manor.
 *
 * @author Daniel Ostermeier
 */
public abstract class CvsForm extends BaseForm
{
    public CvsForm(WebTester tester)
    {
        super(tester);
    }

    /**
     * The create cvs form.
     */
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

        public String[] getFieldNames()
        {
            return new String[]{"cvs.root", "cvs.module", "cvs.password", "cvs.branch", "minutes", "seconds"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD};
        }
    }

    /**
     * The edit cvs form.
     */
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

        public String[] getFieldNames()
        {
            return new String[]{"cvs.root", "cvs.module", "cvs.password", "cvs.branch", "minutes", "seconds", "monitor"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX};
        }
    }
}