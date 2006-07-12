package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class AddProjectWizard
{
    public static class Select extends BaseForm
    {
        public Select(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "project.basics";
        }

        public String[] getFieldNames()
        {
            return new String[]{"name", "description", "url", "scm", "type"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, SELECT, SELECT};
        }
    }
}
