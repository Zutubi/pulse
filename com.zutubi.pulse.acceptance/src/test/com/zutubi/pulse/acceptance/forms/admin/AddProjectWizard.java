package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.AntTypeConfiguration;

/**
 * Encapsulates forms for the steps of the add project wizard.
 */
public class AddProjectWizard
{
    public static class ProjectState extends SeleniumForm
    {
        public ProjectState(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return ProjectConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{"name", "description", "url"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD };
        }
    }

    public static class SubversionState extends SeleniumForm
    {
        public SubversionState(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return "com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration";
        }

        public String[] getFieldNames()
        {
            return new String[]{ "url", "username", "password", "keyfile", "keyfilePassphrase", "checkoutScheme"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, COMBOBOX };
        }
    }

    public static class AntState extends SeleniumForm
    {
        public AntState(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return AntTypeConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{ "work", "file", "target", "args" };
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD };
        }
    }
}
