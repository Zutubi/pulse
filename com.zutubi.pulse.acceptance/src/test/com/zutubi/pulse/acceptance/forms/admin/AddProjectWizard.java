package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.maven.MavenCommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.tove.type.record.PathUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates forms for the steps of the add project wizard.
 */
public class AddProjectWizard
{
    private SeleniumBrowser browser;
    private XmlRpcHelper xmlRpcHelper;

    public AddProjectWizard(SeleniumBrowser browser, XmlRpcHelper xmlRpcHelper)
    {
        this.browser = browser;
        this.xmlRpcHelper = xmlRpcHelper;
    }

    public String addProject(String name)
    {
        return addProject(name, false, ProjectManager.GLOBAL_PROJECT_NAME);
    }

    public String addProject(String name, boolean template, String parentName)
    {
        runAddProjectWizard(new DefaultProjectWizardDriver(parentName, name, template));

        ProjectHierarchyPage hierarchyPage = browser.createPage(ProjectHierarchyPage.class, name, template);
        hierarchyPage.waitFor();

        if (!template)
        {
            try
            {
                xmlRpcHelper.waitForProjectToInitialise(name);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        return PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, name);
    }

    /**
     * Helper method that runs through the WebUI based project creation wizard, using the
     * driver instance to guide the process.
     *
     * @param driver the driver instance
     * @return the final state of the wizard
     */
    public AddProjectWizard.CommandState runAddProjectWizard(ProjectWizardDriver driver)
    {
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, driver.getParentName(), true);
        if (driver.isTemplate())
        {
            hierarchyPage.clickAddTemplate();
        }
        else
        {
            hierarchyPage.clickAdd();
        }

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();

        driver.projectState(projectState);

        SelectTypeState scmTypeState = new SelectTypeState(browser);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements(driver.selectScm());

        AddProjectWizard.ScmState scmState = createScmForm(driver.selectScm());
        scmState.waitFor();

        driver.scmState(scmState);

        String type = driver.selectType();

        ProjectTypeSelectState projectTypeState = new ProjectTypeSelectState(browser);
        projectTypeState.waitFor();
        if (type.equals(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP))
        {
            projectTypeState.nextFormElements(type, driver.selectCommand());
            AddProjectWizard.CommandState commandState = createCommandForm(driver.selectCommand());
            commandState.waitFor();
            driver.commandState(commandState);
            return commandState;
        }
        else
        {
            projectTypeState.nextFormElements(type, null);
            if (!type.equals(ProjectTypeSelectionConfiguration.TYPE_MULTI_STEP))
            {
                AddProjectWizard.TypeState typeState = createTypeForm(type);
                typeState.waitFor();
                driver.typeState(typeState);
            }
            return null;
        }

    }

    private AddProjectWizard.TypeState createTypeForm(String s)
    {
        if (s.equals(ProjectTypeSelectionConfiguration.TYPE_CUSTOM))
        {
            return new AddProjectWizard.CustomTypeState(browser);
        }
        else
        {
            throw new IllegalArgumentException("Unknown type: " + s);
        }
    }

    private AddProjectWizard.CommandState createCommandForm(String s)
    {
        if (s.equals("zutubi.antCommandConfig"))
        {
            return new AddProjectWizard.AntState(browser);
        }
        else if (s.equals("zutubi.mavenCommandConfig"))
        {
            return new AddProjectWizard.MavenState(browser);
        }
        else if (s.equals("zutubi.maven2CommandConfig"))
        {
            return new AddProjectWizard.Maven2State(browser);
        }
        else
        {
            throw new IllegalArgumentException("Unknown command config: " + s);
        }
    }

    private AddProjectWizard.ScmState createScmForm(String s)
    {
        if (s.equals("zutubi.subversionConfig"))
        {
            return new AddProjectWizard.SubversionState(browser);
        }
        else if (s.equals("zutubi.gitConfig"))
        {
            return new AddProjectWizard.GitState(browser);
        }
        else
        {
            throw new IllegalArgumentException("Unknown scm config: " + s);
        }
    }

    /**
     * A callback interface that allows a test case to drive the UI based project
     * creation process.
     */
    public interface ProjectWizardDriver
    {
        /**
         * Callback that allows interaction with the configure project
         *  wizard form.
         *
         * @param form the form instance.
         */
        void projectState(AddProjectWizard.ProjectState form);

        /**
         * @return the symbolic name of the scm to be selected.
         */
        String selectScm();

        /**
         * Callback that allows interaction with the scm wizard form.
         *
         * @param form the form instance.
         */
        void scmState(AddProjectWizard.ScmState form);

        /**
         * @return the type of project, one of the TYPE_* constants in
         * {@link ProjectTypeSelectionConfiguration}.
         */
        String selectType();

        /**
         * @return the symbolic name of the project type to be selected.
         */
        String selectCommand();

        /**
         * Callback that allows interaction with the project type
         * wizard form.
         *
         * @param form the form instance.
         */
        void typeState(AddProjectWizard.TypeState form);

        /**
         * Callback that allows interaction with the project command
         * wizard form.
         *
         * @param form the form instance.
         */
        void commandState(AddProjectWizard.CommandState form);

        String getParentName();

        boolean isTemplate();
    }

    /**
     * The default implementation of the project wizard driver that creates a
     * concrete project using subversion and ant.
     */
    public static class DefaultProjectWizardDriver implements ProjectWizardDriver
    {
        private String name;
        private String parentName;
        private boolean template;

        public DefaultProjectWizardDriver(String parentName, String projectName, boolean template)
        {
            this.name = projectName;
            this.parentName = parentName;
            this.template = template;
        }

        public String getParentName()
        {
            return parentName;
        }

        public boolean isTemplate()
        {
            return template;
        }

        public void projectState(AddProjectWizard.ProjectState form)
        {
            form.nextFormElements(name, "test description", "http://test.com/");
        }

        public String selectScm()
        {
            return "zutubi.subversionConfig";
        }

        public void scmState(AddProjectWizard.ScmState form)
        {
            form.nextFormElements(Constants.TRIVIAL_ANT_REPOSITORY, null, null, null, null, "CLEAN_CHECKOUT");
        }

        public String selectType()
        {
            return ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP;
        }

        public String selectCommand()
        {
            return "zutubi.antCommandConfig";
        }

        public void typeState(AddProjectWizard.TypeState form)
        {
        }

        public void commandState(AddProjectWizard.CommandState form)
        {
            form.finishFormElements("build", null, "build.xml", null, null, null);
        }
    }

    public static class ProjectState extends SeleniumForm
    {
        public ProjectState(SeleniumBrowser browser)
        {
            super(browser);
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
        public ScmState(SeleniumBrowser browser)
        {
            super(browser);
        }
    }

    public static class SubversionState extends ScmState
    {
        public SubversionState(SeleniumBrowser browser)
        {
            super(browser);
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
        public GitState(SeleniumBrowser browser)
        {
            super(browser);
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
        public TypeState(SeleniumBrowser browser)
        {
            super(browser);
        }
    }

    public static class CustomTypeState extends TypeState
    {
        public CustomTypeState(SeleniumBrowser browser)
        {
            super(browser);
        }

        public String getFormName()
        {
            return CustomTypeConfiguration.class.getName();
        }

        public String[] getFieldNames()
        {
            return new String[]{"pulseFileString"};
        }
    }

    public abstract static class CommandState extends SeleniumForm
    {
        protected CommandState(SeleniumBrowser browser)
        {
            super(browser);
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
            return browser.isElementIdPresent("zfid."+fieldName+".browse");
        }
    }

    public static class AntState extends CommandState
    {
        public AntState(SeleniumBrowser browser)
        {
            super(browser);
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
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, ITEM_PICKER };
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
        public MavenState(SeleniumBrowser browser)
        {
            super(browser);
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
        public Maven2State(SeleniumBrowser browser)
        {
            super(browser);
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
            return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, ITEM_PICKER };
        }

        public boolean isBrowseWorkAvailable()
        {
            return isBrowseFieldAvailable("workingDir");
        }
    }
}
