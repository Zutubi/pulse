package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class comment/>
 */
public class AddResourceWizard
{
    public static class Select extends BaseForm
    {
        public Select(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "resource.add";
        }

        public String[] getFieldNames()
        {
            return new String[]{"type"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{SELECT};
        }
    }

    public static class Custom extends BaseForm
    {
        public Custom(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "resource.add.custom";
        }

        public String[] getFieldNames()
        {
            return new String[]{"name"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{TEXTFIELD};
        }
    }
}
