package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.admin.ProjectTypeSelectState;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.tove.type.record.PathUtils;

/**
 * Created by IntelliJ IDEA.
 * User: daniel
 * Date: 14/11/2010
 * Time: 6:06:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebUIHelper
{
    private SeleniumBrowser browser;
    private XmlRpcHelper xmlRpcHelper;

    public WebUIHelper(SeleniumBrowser browser, XmlRpcHelper xmlRpcHelper)
    {
        this.browser = browser;
        this.xmlRpcHelper = xmlRpcHelper;
    }

    protected String addProject(String name)
    {
        return addProject(name, false, ProjectManager.GLOBAL_PROJECT_NAME);
    }

    protected String addProject(String name, boolean template, String parentName)
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

}
