package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class comment/>
 */
public class AddCommitMessageTransformerWizard
{
    public static class Select extends BaseForm
    {
        public Select(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "SelectType";
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

    public static class Standard extends BaseForm
    {
        public Standard(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "StandardHandler";
        }

        public String[] getFieldNames()
        {
            return new String[]{"name", "expression", "link"};
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
            return "CustomHandler";
        }

        public String[] getFieldNames()
        {
            return new String[]{"name", "expression", "replacement"};
        }
    }

    public static class Jira extends BaseForm
    {
        public Jira(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "JiraHandler";
        }

        public String[] getFieldNames()
        {
            return new String[]{"name", "url"};
        }
    }
}
