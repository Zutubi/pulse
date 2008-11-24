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
    public void testMavenDefaultTestArtifactConfiguration() throws Exception
    {
        createMavenProject();

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> artifact = getArtifactConfiguration(random, "test reports");
        assertArtifactConfiguration(artifact, "test reports", "target/test-reports", "TEXT-*.xml", "junit");
    }

    public void testMaven2DefaultTestArtifactConfiguration() throws Exception
    {
        createMaven2Project();

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> artifact = getArtifactConfiguration(random, "test reports");
        assertArtifactConfiguration(artifact, "test reports", "target/surefire-reports", "TEXT-*.xml", "junit");
    }

    public void testMaven2BuildPicksUpTests() throws Exception
    {
        createMaven2Project();

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

    @SuppressWarnings({ "unchecked" })
    private void assertArtifactConfiguration(Hashtable<String, Object> artifact, String expectedName, String expectedBase, String expectedIncludes, String... expectedPostProcessors)
    {
        Vector<String> postprocessors = (Vector<String>) artifact.get(Constants.DirectoryArtifact.POSTPROCESSORS);
        assertEquals(expectedPostProcessors.length, postprocessors.size());
        for (int i = 0; i < expectedPostProcessors.length; i++)
        {
            assertEquals(expectedPostProcessors[i], postprocessors.get(i));
        }
        assertEquals(expectedName, artifact.get(Constants.DirectoryArtifact.NAME));
        assertEquals(expectedBase, artifact.get(Constants.DirectoryArtifact.BASE));
        assertEquals(expectedIncludes, artifact.get(Constants.DirectoryArtifact.INCLUDES));
    }

    private void createMavenProject() throws Exception
    {
        Hashtable<String, Object> type = xmlRpcHelper.createEmptyConfig("zutubi.mavenTypeConfig");
        type.put("targets", "install");

        createMavenProject(type);
    }

    private void createMaven2Project() throws Exception
    {
        Hashtable<String, Object> type = xmlRpcHelper.createEmptyConfig("zutubi.maven2TypeConfig");
        type.put("goals", "install");

        createMavenProject(type);
    }

    private void createMavenProject(Hashtable<String, Object> type) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.TEST_MAVEN_REPOSITORY), type);
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
