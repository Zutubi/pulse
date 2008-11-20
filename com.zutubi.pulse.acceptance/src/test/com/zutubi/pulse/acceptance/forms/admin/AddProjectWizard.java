package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.AntTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.Maven2TypeConfiguration;

import java.util.List;
import java.util.LinkedList;

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

    public abstract static class ScmState extends SeleniumForm
    {
        public ScmState(Selenium selenium)
        {
            super(selenium);
        }
    }

    public static class SubversionState extends ScmState
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

    public static class GitState extends ScmState
    {
        public GitState(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return "com.zutubi.pulse.core.scm.git.config.GitConfiguration";
        }

        public String[] getFieldNames()
        {
            return new String[]{ "repository", "branch", "checkoutScheme"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD};
        }
    }

    public abstract static class TypeState extends SeleniumForm
    {
        protected TypeState(Selenium selenium)
        {
            super(selenium);
        }

        public void cancel()
        {
            List<String> emptyValues = new LinkedList<String>();
            for (int type : getFieldTypes())
            {
                switch (type)
                {
                    case TEXTFIELD:
                        emptyValues.add("");
                        break;
                    case CHECKBOX:
                        emptyValues.add("false");
                        break;
                }
            }

            cancelFormElements(emptyValues.toArray(new String[emptyValues.size()]));
        }

        protected boolean isBrowseFieldAvailable(String fieldName)
        {
            return selenium.isElementPresent("zfid."+fieldName+".browse");
        }
    }

    public static class AntState extends TypeState
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

        public boolean isBrowseWorkAvailable()
        {
            return isBrowseFieldAvailable("work");
        }

        public boolean isBrowseFileAvailable()
        {
            return isBrowseFieldAvailable("file");
        }
    }

    public static class Maven2State extends TypeState
    {
        public Maven2State(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return Maven2TypeConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{"workingDir", "goals", "arguments"};
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD};
        }

        public boolean isBrowseWorkAvailable()
        {
            return isBrowseFieldAvailable("workingDir");
        }
    }
}
