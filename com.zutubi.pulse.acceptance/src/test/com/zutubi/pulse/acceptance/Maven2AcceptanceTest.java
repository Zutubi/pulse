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

    @SuppressWarnings({ "unchecked" })
    public void testSurefireTestsArtifactConfiguration() throws Exception
    {
        createMavenProject(random);

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> artifact = getArtifactConfiguration(random, "test reports");
        assertEquals("test reports", artifact.get(Constants.DirectoryArtifact.NAME));
        assertEquals("target/surefire-reports", artifact.get(Constants.DirectoryArtifact.BASE));
        assertEquals("TEST-*.xml", artifact.get(Constants.DirectoryArtifact.INCLUDES));
        Vector<String> postprocessors = (Vector<String>) artifact.get(Constants.DirectoryArtifact.POSTPROCESSORS);
        assertEquals(1, postprocessors.size());
        assertEquals("junit", postprocessors.get(0));
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
        try
        {
            xmlRpcHelper.loginAsAdmin();
            return xmlRpcHelper.runBuild(projectName, 30000);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    private Hashtable<String, Object> getArtifactConfiguration(String projectName, String artifactName) throws Exception
    {
        try
        {
            xmlRpcHelper.loginAsAdmin();
            return xmlRpcHelper.getProjectArtifact(projectName, artifactName);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }
}
