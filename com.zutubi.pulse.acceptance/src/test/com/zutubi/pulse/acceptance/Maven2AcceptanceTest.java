package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.DirectoryArtifactForm;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.types.DirectoryArtifactConfiguration;

import java.util.Hashtable;

/**
 * The acceptance tests for the Pulse builtin maven integration.
 */
public class Maven2AcceptanceTest extends SeleniumTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();

        super.tearDown();
    }

    public void testSurefireTestsArtifactConfiguration() throws Exception
    {
        createMavenProject(random);

        // We expect a artifact called surefire-reports to be configured.
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        CompositePage type = configPage.clickComposite("type", "maven 2 command and artifacts");
        type.waitFor();
        type.expandTreeNode(random + "/type");
        ListPage artifacts = type.clickCollection("artifacts", "artifacts");
        artifacts.waitFor();
        artifacts.assertItemPresent("surefire-reports", null, "view", "clone", "delete");
        artifacts.clickView("surefire-reports");

        DirectoryArtifactForm artifact = new DirectoryArtifactForm(selenium, DirectoryArtifactConfiguration.class);
        artifact.waitFor();
        
        assertTrue(artifact.isFormPresent());
        assertEquals("surefire-reports", artifact.getNameFieldValue());
        assertEquals("target/surefire-reports", artifact.getBaseDirectoryFieldValue());
        assertEquals("*.xml", artifact.getIncludesFieldValue());
        
        String[] selectedPostProcessors = artifact.getSelectedPostProcessorValues();
        assertEquals(1, selectedPostProcessors.length);
        assertEquals("junit", selectedPostProcessors[0]);
    }

    public void testMaven2BuildPicksUpTests() throws Exception
    {
        createMavenProject(random);

        int buildNumber = runBuild(random);

        // We expect the summary page to report that 1 test passed.
        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, random, buildNumber);
        summaryPage.goTo();
        assertTrue(selenium.isElementPresent("link=*all 1 passed*"));

        // We expect the artifacts page to contain an artifact called surefire-reports.
        BuildArtifactsPage artifactsPage = new BuildArtifactsPage(selenium, urls, random, buildNumber);
        artifactsPage.goTo();
        SeleniumUtils.waitForLocator(selenium, artifactsPage.getArtifactLocator("surefire-reports"));
    }

    private void createMavenProject(String projectName) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();

        Hashtable<String, Object> scm = xmlRpcHelper.getSubversionConfig();
        scm.put("url", Constants.TEST_MAVEN_REPOSITORY);
        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, scm, xmlRpcHelper.getMaven2Config());

        xmlRpcHelper.waitForProjectToInitialise(projectName);
        xmlRpcHelper.logout();
    }

    private int runBuild(String projectName) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.triggerBuild(projectName);

        Hashtable<String, Object> build;
        do
        {
            build = xmlRpcHelper.getBuild(projectName, 1);
        }
        while (build == null || !Boolean.TRUE.equals(build.get("completed")));

        xmlRpcHelper.logout();

        return (Integer)build.get("id");
    }

}
