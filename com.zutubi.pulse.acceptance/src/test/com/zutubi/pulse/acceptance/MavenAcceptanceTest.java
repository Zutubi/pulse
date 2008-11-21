package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.model.ProjectManager;

import java.util.Hashtable;
import java.util.Vector;

/**
 * The acceptance tests for the Pulse builtin maven 1.x and 2.x integration.
 */
public class MavenAcceptanceTest extends SeleniumTestBase
{
    @SuppressWarnings({ "unchecked" })
    public void testMavenDefaultTestArtifactConfiguration() throws Exception
    {
        createMavenProject(random);

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> artifact = getArtifactConfiguration(random, "test reports");
        assertEquals("test reports", artifact.get(Constants.DirectoryArtifact.NAME));
        assertEquals("target/test-reports", artifact.get(Constants.DirectoryArtifact.BASE));
        assertEquals("TEST-*.xml", artifact.get(Constants.DirectoryArtifact.INCLUDES));
        Vector<String> postprocessors = (Vector<String>) artifact.get(Constants.DirectoryArtifact.POSTPROCESSORS);
        assertEquals(1, postprocessors.size());
        assertEquals("junit", postprocessors.get(0));
    }

    @SuppressWarnings({ "unchecked" })
    public void testMaven2DefaultTestArtifactConfiguration() throws Exception
    {
        createMaven2Project(random);

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
        createMaven2Project(random);

        int buildNumber = runBuild(random);

        loginAsAdmin();

        // We expect the summary page to report that 1 test passed.
        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, random, buildNumber);
        summaryPage.goTo();
        assertEquals("all 1 passed", summaryPage.getSummaryTestsColumnText());

        // We expect the artifacts page to contain an artifact called test reports.
        BuildArtifactsPage artifactsPage = new BuildArtifactsPage(selenium, urls, random, buildNumber);
        artifactsPage.goTo();
        SeleniumUtils.waitForLocator(selenium, artifactsPage.getArtifactLocator("test reports"));
    }

    private void createMavenProject(String projectName) throws Exception
    {
        Hashtable<String, Object> type = xmlRpcHelper.createEmptyConfig("zutubi.mavenTypeConfig");
        type.put("targets", "install");

        createMavenProject(projectName, type);
    }

    private void createMaven2Project(String projectName) throws Exception
    {
        Hashtable<String, Object> type = xmlRpcHelper.createEmptyConfig("zutubi.maven2TypeConfig");
        type.put("goals", "install");

        createMavenProject(projectName, type);
    }

    private void createMavenProject(String projectName, Hashtable<String, Object> type) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();

        Hashtable<String, Object> scm = xmlRpcHelper.getSubversionConfig();
        scm.put("url", Constants.TEST_MAVEN_REPOSITORY);
        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, scm, type);

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
