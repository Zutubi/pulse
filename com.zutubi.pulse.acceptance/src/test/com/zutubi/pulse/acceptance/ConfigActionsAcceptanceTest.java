package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AntTypeForm;
import com.zutubi.pulse.acceptance.forms.admin.CustomTypeForm;
import com.zutubi.pulse.acceptance.forms.admin.SetPasswordForm;
import com.zutubi.pulse.acceptance.forms.admin.UserForm;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.admin.AgentConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Hashtable;

/**
 * Acceptance tests for actions that may be executed on configuration
 * instances.
 */
@Test(dependsOnGroups = {"init.*"})
public class ConfigActionsAcceptanceTest extends SeleniumTestBase
{
    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testCustomActionWithArgument() throws Exception
    {
        ListPage usersPage = customActionWithArgumentPrelude();

        SetPasswordForm form = new SetPasswordForm(selenium);
        form.waitFor();
        form.saveFormElements("testpw", "testpw");

        usersPage.waitFor();
        logout();

        // Login with the new password
        login(random, "testpw");
        selenium.waitForPageToLoad("30000");
        WelcomePage welcomePage = new WelcomePage(selenium, urls);
        assertTrue(welcomePage.isPresent());
        assertTitle(welcomePage);
    }

    public void testCustomActionWithArgumentValidation() throws Exception
    {
        customActionWithArgumentPrelude();
        SetPasswordForm form = new SetPasswordForm(selenium);
        form.waitFor();
        form.saveFormElements("one", "two");
        form.waitFor();
        assertTextPresent("passwords do not match");
    }

    public void testCustomActionWithArgumentCancel() throws Exception
    {
        customActionWithArgumentPrelude();

        SetPasswordForm setPasswordForm = new SetPasswordForm(selenium);
        setPasswordForm.waitFor();
        setPasswordForm.cancelFormElements("testpw", "testpw");

        UserForm userForm = new UserForm(selenium, random);
        userForm.waitFor();
        logout();

        // Check the password is unchanged
        login(random, "");
        selenium.waitForPageToLoad("30000");
        WelcomePage welcomePage = new WelcomePage(selenium, urls);
        assertTrue(welcomePage.isPresent());
        assertTitle(welcomePage);
    }

    private ListPage customActionWithArgumentPrelude() throws Exception
    {
        xmlRpcHelper.insertTrivialUser(random);

        loginAsAdmin();
        ListPage usersPage = new ListPage(selenium, urls, ConfigurationRegistry.USERS_SCOPE);
        usersPage.goTo();
        usersPage.clickAction(random, "setPassword");
        return usersPage;
    }

    public void testPrepareAction() throws Exception
    {
        ProjectConfigPage projectPage = prepareActionPrelude();

        CustomTypeForm form = new CustomTypeForm(selenium);
        form.waitFor();

        // Make sure the arg was prepared from the current project config
        assertTrue(form.getFieldValue("pulseFileString").contains("pull in the ant resource"));
        form.saveFormElements(new String[]{null});

        projectPage.waitFor();
        SeleniumUtils.waitForLocator(selenium, projectPage.getTreeLinkLocator("custom pulse file"));
        projectPage.clickComposite("type", "custom pulse file");

        form.waitFor();
        assertTrue(form.getFieldValue("pulseFileString").contains("pull in the ant resource"));
    }

    public void testPrepareActionCancel() throws Exception
    {
        ProjectConfigPage projectPage = prepareActionPrelude();

        CustomTypeForm customForm = new CustomTypeForm(selenium);
        customForm.waitFor();
        customForm.cancelFormElements(new String[]{null});

        projectPage.waitFor();
        assertFalse(projectPage.isTreeLinkPresent("pulse file"));
        assertTrue(projectPage.isTreeLinkPresent("ant command and artifacts"));

        projectPage.clickComposite("type", "ant command and artifacts");
        AntTypeForm antForm = new AntTypeForm(selenium);
        antForm.waitFor();
    }

    public void testPrepareActionValidation() throws Exception
    {
        prepareActionPrelude();

        CustomTypeForm form = new CustomTypeForm(selenium);
        form.waitFor();

        // Make sure the arg was prepared from the current project config
        assertTrue(form.getFieldValue("pulseFileString").contains("pull in the ant resource"));
        form.saveFormElements("<?xml version=\"1.0\"?><project><nosuchtag/></project>");
        form.waitFor();

        assertTextPresent("Undefined type 'nosuchtag'");
    }

    private ProjectConfigPage prepareActionPrelude() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();
        ProjectConfigPage projectPage = new ProjectConfigPage(selenium, urls, random, false);
        projectPage.goTo();
        projectPage.clickAction(ProjectConfigurationActions.ACTION_CONVERT_TO_CUSTOM);
        return projectPage;
    }

    public void testCannotConvertProjectWhenTypeIsInherited() throws Exception
    {
        String parentProject = random + "-parent";
        String childProject = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentProject, true);
        xmlRpcHelper.insertTrivialProject(childProject, parentProject, false);

        loginAsAdmin();
        ProjectConfigPage projectPage = new ProjectConfigPage(selenium, urls, childProject, false);
        projectPage.goTo();
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_CUSTOM));
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_VERSIONED));
    }

    public void testCannotConvertProjectWhenTypeIsOverridden() throws Exception
    {
        String parentProject = random + "-parent";
        String childProject = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentProject, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childProject, parentProject, false);

        loginAsAdmin();
        ProjectConfigPage projectPage = new ProjectConfigPage(selenium, urls, parentProject, false);
        projectPage.goTo();
        assertTrue(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_CUSTOM));
        assertTrue(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_VERSIONED));

        String childTypePath = PathUtils.getPath(childPath, "type");
        Hashtable<String, Object> childType = xmlRpcHelper.getConfig(childTypePath);
        childType.put("file", "meoverridenow");
        xmlRpcHelper.saveConfig(childTypePath, childType, false);

        projectPage.goTo();
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_CUSTOM));
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_VERSIONED));
    }

    public void testActionsListedForAgent() throws Exception
    {
        loginAsAdmin();
        AgentConfigPage agentPage = new AgentConfigPage(selenium, urls, AgentManager.MASTER_AGENT_NAME, false);
        agentPage.goTo();
        assertTrue(agentPage.isActionPresent(AgentConfigurationActions.ACTION_PING));
    }

    public void testActionsNotListedForTemplateAgent() throws Exception
    {
        loginAsAdmin();
        AgentConfigPage agentPage = new AgentConfigPage(selenium, urls, AgentManager.GLOBAL_AGENT_NAME, false);
        agentPage.goTo();
        assertFalse(agentPage.isActionPresent(AgentConfigurationActions.ACTION_PING));
    }

    public void testActionsNotListedForInvalidProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectConfigPage projectPage = new ProjectConfigPage(selenium, urls, random, false);
        projectPage.goTo();
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_TRIGGER));
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_CUSTOM));
    }

    public void testActionsListedForTemplateProject() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, true);

        loginAsAdmin();
        ProjectConfigPage projectPage = new ProjectConfigPage(selenium, urls, random, false);
        projectPage.goTo();
        assertFalse(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_TRIGGER));
        assertTrue(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_CUSTOM));
        assertTrue(projectPage.isActionPresent(ProjectConfigurationActions.ACTION_CONVERT_TO_VERSIONED));
    }
}
