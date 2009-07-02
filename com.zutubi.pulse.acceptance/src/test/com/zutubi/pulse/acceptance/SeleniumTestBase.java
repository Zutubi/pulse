package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.admin.ProjectTypeSelectState;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import junit.framework.Assert;
import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * Helper base class for web UI acceptance tests that use Selenium.
 */
public class SeleniumTestBase extends PulseTestCase
{
    private static final long STATUS_TIMEOUT = 30000;

    /**
     * Shared agent used for simple single-agent builds.  Makes it easier to
     * run these tests in development environments (just manually run one
     * agent on port 8890).
     */
    protected static final String AGENT_NAME = "localhost";

    protected Urls urls;
    protected String random;
    protected XmlRpcHelper xmlRpcHelper;

    protected SeleniumBrowser browser;

    public SeleniumTestBase()
    {
        super();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper = new XmlRpcHelper();
        random = getName() + "-" + RandomUtils.randomString(10);

        browser = new SeleniumBrowser();
        browser.start();

        urls = browser.getUrls();
    }

    protected void tearDown() throws Exception
    {
        browser.stop();
        super.tearDown();
    }

    protected void login(String username, String password)
    {
        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        page.login(username, password);
    }

    protected void loginAsAdmin()
    {
        login("admin", "admin");
    }

    protected void logout()
    {
        browser.click("logout");
        browser.waitForPageToLoad();
    }
    
    protected void assertElementPresent(String id)
    {
        assertTrue("No element with id '" + id + "' found", browser.isElementIdPresent(id));
    }

    protected void assertElementNotPresent(String id)
    {
        assertFalse("Unexpected element with id '" + id + "' found", browser.isElementIdPresent(id));
    }

    protected void assertTextPresent(String text)
    {
        assertTrue(browser.isTextPresent(text));
    }

    protected void assertTextNotPresent(String text)
    {
        assertFalse(browser.isTextPresent(text));
    }

    protected void assertLinkPresent(String id)
    {
        assertTrue(browser.isLinkPresent(id));
    }

    protected void assertFormFieldNotEmpty(String id)
    {
        String value = browser.getValue(StringUtils.toValidHtmlName(id));
        assertNotNull(value);
        assertTrue(value.length() > 0);
    }

    protected void assertGenericError(String message)
    {
        assertElementPresent("generic-error");
        assertTextPresent(message);
    }

    protected void waitForStatus(String message)
    {
        browser.waitForElement(IDs.STATUS_MESSAGE, STATUS_TIMEOUT);
        AcceptanceTestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return TextUtils.stringSet(browser.getText(IDs.STATUS_MESSAGE));
            }
        }, STATUS_TIMEOUT, "status message to be set.");

        String text = browser.getText(IDs.STATUS_MESSAGE);
        assertThat(text, containsString(message));        
    }

    protected String addProject(String name, boolean useAPI)
    {
        return addProject(name, false, ProjectManager.GLOBAL_PROJECT_NAME, useAPI);
    }

    protected String addProject(String name, boolean template, String parentName, boolean useAPI)
    {
        if (useAPI)
        {
            try
            {
                xmlRpcHelper.insertSimpleProject(name, parentName, template);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        else
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
        }

        return PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, name);
    }

    protected boolean ensureProject(final String name) throws Exception
    {
        final boolean[] result = new boolean[1];
        doRpc(new ExceptionWrappingRunnable()
        {
            public void innerRun() throws Exception
            {
                result[0] = xmlRpcHelper.ensureProject(name);
            }
        });

        return result[0];
    }

    protected void ensureAgent(final String name) throws Exception
    {
        doRpc(new ExceptionWrappingRunnable()
        {
            public void innerRun() throws Exception
            {
                xmlRpcHelper.ensureAgent(name);
            }
        });
    }

    private void doRpc(Runnable runnable) throws Exception
    {
        boolean loggedIn = false;
        if (!xmlRpcHelper.isLoggedIn())
        {
            xmlRpcHelper.loginAsAdmin();
            loggedIn = true;
        }

        try
        {
            runnable.run();
        }
        finally
        {
            if (loggedIn)
            {
                xmlRpcHelper.logout();
            }
        }
    }

    protected String getNewestListItem(String labelsPath) throws Exception
    {
        Vector<String> labels = xmlRpcHelper.call("getConfigListing", labelsPath);
        Collections.sort(labels, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                long h1 = Long.parseLong(o1);
                long h2 = Long.parseLong(o2);
                return (int) (h1 - h2);
            }
        });
        return labels.get(labels.size() - 1);
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

    public void assertFormElements(SeleniumForm form, String... values)
    {
        TestCase.assertTrue(form.isFormPresent());

        int[] types = form.getActualFieldTypes();
        Assert.assertEquals(values.length, types.length);

        for (int i = 0; i < types.length; i++)
        {
            if (values[i] != null)
            {
                String fieldName = form.getActualFieldNames()[i];
                switch (types[i])
                {
                    case SeleniumForm.TEXTFIELD:
                        TestCase.assertEquals(StringUtils.stripLineBreaks(values[i]), StringUtils.stripLineBreaks(form.getFieldValue(fieldName)));
                        break;
                    case SeleniumForm.CHECKBOX:
                        TestCase.assertEquals(Boolean.valueOf(values[i]) ? "on" : "off", form.getFieldValue(fieldName));
                        break;
                    case SeleniumForm.COMBOBOX:
                        TestCase.assertEquals(values[i], form.getFieldValue(fieldName));
                        break;
                    case SeleniumForm.ITEM_PICKER:
                    case SeleniumForm.MULTI_CHECKBOX:
                    case SeleniumForm.MULTI_SELECT:
                        if (values[i] != null)
                        {
                            String[] expected = form.convertMultiValue(values[i]);

                            String fieldValue = form.getFieldValue(fieldName);
                            String[] gotValues = fieldValue.length() == 0 ? new String[0] : fieldValue.split(",");
                            Assert.assertEquals(expected.length, gotValues.length);
                            for (int j = 0; j < expected.length; j++)
                            {
                                Assert.assertEquals(expected[j], gotValues[j]);
                            }
                        }
                    default:
                        break;
                }
            }
        }
    }

    public void assertTitle(SeleniumPage page)
    {
        if (page.getTitle() != null)
        {
            String gotTitle = browser.getTitle();
            if(gotTitle.startsWith(SeleniumPage.TITLE_PREFIX))
            {
                gotTitle = gotTitle.substring(SeleniumPage.TITLE_PREFIX.length());
            }
            Assert.assertEquals(page.getTitle(), gotTitle);
        }
    }

    protected void assertItemPresent(ListPage page, String baseName, String annotation, String... actions)
    {
        assertTrue(page.isItemPresent(baseName));

        if(annotation == null)
        {
            annotation = "noan";
        }
        assertTrue(page.isAnnotationPresent(baseName, annotation));

        for(String action: actions)
        {
            assertTrue(page.isActionLinkPresent(baseName, action));
        }
    }
}
