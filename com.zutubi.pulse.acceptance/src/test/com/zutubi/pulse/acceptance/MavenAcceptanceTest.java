package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * The acceptance tests for the Pulse builtin maven 1.x and 2.x integration.
 */
public class MavenAcceptanceTest extends SeleniumTestBase
{
    private static final int BUILD_TIMEOUT = 90000;
    private static final String COMMAND_NAME = "build";
    private static final String JUNIT_PROCESSOR_NAME = "junit xml report processor";

    public void testMavenDefaultTestCaptureConfiguration() throws Exception
    {
        loginAsAdmin();
        createMavenProject();

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> capture = getCaptureConfiguration(random, "test reports");
        assertNotNull(capture);
        assertCaptureConfiguration(capture, "test reports", "target/test-reports", "TEST-*.xml", JUNIT_PROCESSOR_NAME);
    }

    public void testMaven2DefaultTestArtifactConfiguration() throws Exception
    {
        loginAsAdmin();
        createMaven2Project();

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> capture = getCaptureConfiguration(random, "test reports");
        assertNotNull(capture);
        assertCaptureConfiguration(capture, "test reports", "target/surefire-reports", "TEST-*.xml", JUNIT_PROCESSOR_NAME);
    }

    public void testMaven2BuildPicksUpTests() throws Exception
    {
        loginAsAdmin();
        createMaven2Project();

        int buildNumber = runBuild(random);

        // We expect the summary page to report that 1 test passed.
        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, random, buildNumber);
        summaryPage.goTo();
        assertEquals("1 passed", summaryPage.getSummaryTestsColumnText());

        // We expect the artifacts page to contain an artifact called test reports.
        BuildArtifactsPage artifactsPage = new BuildArtifactsPage(selenium, urls, random, buildNumber);
        artifactsPage.goTo();
        SeleniumUtils.waitForLocator(selenium, artifactsPage.getArtifactLocator("test reports"));
    }

    @SuppressWarnings({ "unchecked" })
    private void assertCaptureConfiguration(Hashtable<String, Object> capture, String expectedName, String expectedBase, String expectedIncludes, String... expectedPostProcessors)
    {
        Vector<String> postprocessors = (Vector<String>) capture.get(Constants.DirectoryOutput.POSTPROCESSORS);
        assertEquals(expectedPostProcessors.length, postprocessors.size());
        for (int i = 0; i < expectedPostProcessors.length; i++)
        {
            assertEquals(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, "postProcessors", expectedPostProcessors[i]), postprocessors.get(i));
        }
        assertEquals(expectedName, capture.get(Constants.DirectoryOutput.NAME));
        assertEquals(expectedBase, capture.get(Constants.DirectoryOutput.BASE));
        assertEquals(expectedIncludes, ((Vector) capture.get(Constants.DirectoryOutput.INCLUSIONS)).get(0));
    }

    private void createMavenProject() throws Exception
    {
        createMavenProject("zutubi.mavenCommandConfig", asMap(asPair("targets", "install")));
    }

    private void createMaven2Project() throws Exception
    {
        createMavenProject("zutubi.maven2CommandConfig", asMap(asPair("goals", "install")));
    }

    private void createMavenProject(final String commandType, final Map<String, String> fieldValues) throws Exception
    {
        runAddProjectWizard(new DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
        {
            @Override
            public void scmState(AddProjectWizard.ScmState form)
            {
                form.nextFormElements(asMap(asPair("url", Constants.TEST_MAVEN_REPOSITORY)));
            }

            @Override
            public String selectCommand()
            {
                return commandType;
            }

            @Override
            public void commandState(AddProjectWizard.CommandState form)
            {
                fieldValues.put("name", COMMAND_NAME);
                form.finishFormElements(fieldValues);
            }
        });

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.waitFor();
    }

    private int runBuild(String projectName) throws Exception
    {
        try
        {
            xmlRpcHelper.loginAsAdmin();
            return xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    private Hashtable<String, Object> getCaptureConfiguration(String projectName, String artifactName) throws Exception
    {
        try
        {
            xmlRpcHelper.loginAsAdmin();
            return xmlRpcHelper.getProjectCapture(projectName, "default", COMMAND_NAME, artifactName);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }
}
