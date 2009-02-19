package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.maven.MavenCommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.LinkedList;
import java.util.List;

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
            return SubversionConfiguration.class.getName();
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

    public abstract static class CommandState extends SeleniumForm
    {
        protected CommandState(Selenium selenium)
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
                    case ITEM_PICKER:
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

    public static class AntState extends CommandState
    {
        public AntState(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return AntCommandConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{ "name", "workingDir", "buildFile", "targets", "args", "postProcessors" };
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, ITEM_PICKER };
        }

        public boolean isBrowseWorkAvailable()
        {
            return isBrowseFieldAvailable("workingDir");
        }

        public boolean isBrowseFileAvailable()
        {
            return isBrowseFieldAvailable("buildFile");
        }
    }

    public static class MavenState extends CommandState
    {
        public MavenState(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return MavenCommandConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{ "name", "workingDir", "projectFile", "targets", "args", "postProcessors" };
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, ITEM_PICKER };
        }

        public boolean isBrowseWorkAvailable()
        {
            return isBrowseFieldAvailable("workingDir");
        }
    }

    public static class Maven2State extends CommandState
    {
        public Maven2State(Selenium selenium)
        {
            super(selenium);
        }

        public String getFormName()
        {
            return Maven2CommandConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{ "name", "workingDir", "pomFile", "goals", "args", "postProcessors" };
        }

        public int[] getFieldTypes()
        {
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, ITEM_PICKER };
        }

        public boolean isBrowseWorkAvailable()
        {
            return isBrowseFieldAvailable("workingDir");
        }
    }
}
