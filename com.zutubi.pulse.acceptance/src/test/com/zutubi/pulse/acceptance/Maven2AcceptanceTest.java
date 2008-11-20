package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.model.ProjectManager;

import java.util.Hashtable;
import java.util.Vector;

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

    public void testSurefireTestsArtifactConfiguration() throws Exception
    {
        createMavenProject(random);

        // We expect a artifact called surefire-reports to be configured.
        xmlRpcHelper.loginAsAdmin();
        Hashtable<String, Object> projectConfig = xmlRpcHelper.getConfig("projects/" + random);
        Hashtable<String, Object> projectType = (Hashtable<String, Object>) projectConfig.get("type");
        Hashtable<String, Object> typeArtifacts = (Hashtable<String, Object>) projectType.get("artifacts");
        Hashtable<String, Object> artifactConfig = (Hashtable<String, Object>) typeArtifacts.get("test reports");
        assertEquals("test reports", artifactConfig.get("name"));
        assertEquals("target/surefire-reports", artifactConfig.get("base"));
        assertEquals("TEST-*.xml", artifactConfig.get("includes"));
        Vector<String> postprocessors = (Vector<String>) artifactConfig.get("postprocessors");
        assertEquals(1, postprocessors.size());
        assertEquals("junit", postprocessors.get(0));
        xmlRpcHelper.logout();
    }

    public void testMaven2BuildPicksUpTests() throws Exception
    {
        createMavenProject(random);

        int buildNumber = runBuild(random);

        // We expect the summary page to report that 1 test passed.
        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, random, buildNumber);
        summaryPage.goTo();
        assertTrue(selenium.isElementPresent("link=*all 1 passed*"));

        // We expect the artifacts page to contain an artifact called test reports.
        BuildArtifactsPage artifactsPage = new BuildArtifactsPage(selenium, urls, random, buildNumber);
        artifactsPage.goTo();
        SeleniumUtils.waitForLocator(selenium, artifactsPage.getArtifactLocator("test reports"));
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
