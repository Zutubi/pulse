package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.config.ConfigurationRegistry;

import java.util.Hashtable;

/**
 * Simple sanity checks for personal builds.
 */
public class PersonalBuildAcceptanceTest extends SeleniumTestBase
{
    private static final String AGENT_NAME   = "localhost";
    private static final String PROJECT_NAME = "PersonalBuildAcceptanceTest-Project";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testPersonalBuild() throws Exception
    {
        loginAsAdmin();
        MyBuildsPage myBuildsPage = new MyBuildsPage(selenium, urls);
        
        goTo(urls.adminProjects());
        addProject(random);

//        PersonalBuildCommand
//        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);
    }
}
