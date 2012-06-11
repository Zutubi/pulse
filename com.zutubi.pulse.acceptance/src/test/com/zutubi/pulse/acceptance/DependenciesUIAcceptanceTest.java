package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildDetailsPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectDependenciesPage;
import com.zutubi.pulse.acceptance.pages.browse.StageLogPage;
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.DepAntProject;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurationHelper;
import com.zutubi.pulse.acceptance.utils.Repository;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.util.Condition;
import com.zutubi.util.WebUtils;

import static java.lang.String.valueOf;

public class DependenciesUIAcceptanceTest extends AcceptanceTestBase
{
    private Repository repository;
    private String randomName;
    private BuildRunner buildRunner;

    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();

        randomName = randomName();

        repository = new Repository();
        repository.clean();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();

        super.tearDown();
    }

    private void insertProject(ProjectConfigurationHelper project) throws Exception
    {
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
    }

    public void testBuildDetailsReport() throws Exception
    {
        getBrowser().loginAsAdmin();

        DepAntProject projectA = projectConfigurations.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifactA.jar");
        projectA.addFilesToCreate("build/artifactA.jar");
        insertProject(projectA);
        long projectABuildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        BuildDetailsPage detailsPage = getBrowser().openAndWaitFor(BuildDetailsPage.class, projectA.getName(), projectABuildNumber);
        assertFalse(detailsPage.isDependenciesTablePresent());

        DepAntProject projectB = projectConfigurations.createDepAntProject(randomName + "B");
        projectB.addArtifacts("build/artifactB.jar");
        projectB.addFilesToCreate("build/artifactB.jar");
        insertProject(projectB);
        long projectBBuildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        DepAntProject dependentProject = projectConfigurations.createDepAntProject(randomName + "C");
        dependentProject.addDependency(projectA);
        dependentProject.addDependency(projectB);
        insertProject(dependentProject);
        long buildNumber = buildRunner.triggerSuccessfulBuild(dependentProject.getConfig());

        detailsPage = getBrowser().openAndWaitFor(BuildDetailsPage.class, dependentProject.getName(), buildNumber);
        assertTrue(detailsPage.isDependenciesTablePresent());
        
        BuildDetailsPage.DependencyRow row1 = detailsPage.getDependencyRow(1);
        BuildDetailsPage.DependencyRow row2 = detailsPage.getDependencyRow(2);

        assertEquals("default", row1.getStage());
        assertEquals("default", row2.getStage());

        assertEquals(projectA.getName(), row1.getProject());
        assertEquals(valueOf(projectABuildNumber), row1.getBuild());
        assertEquals("artifactA.jar", row1.getArtifact());

        assertEquals(projectB.getName(), row2.getProject());
        assertEquals(valueOf(projectBBuildNumber), row2.getBuild());
        assertEquals("artifactB.jar", row2.getArtifact());
    }

    public void testBuildArtifactLinksIfAvailable() throws Exception
    {
        getBrowser().loginAsAdmin();

        DepAntProject projectA = projectConfigurations.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifactA.jar");
        projectA.addFilesToCreate("build/artifactA.jar");
        insertProject(projectA);
        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject dependentProject = projectConfigurations.createDepAntProject(randomName + "C");
        dependentProject.addDependency(projectA);
        insertProject(dependentProject);
        long buildNumber = buildRunner.triggerSuccessfulBuild(dependentProject.getConfig());

        getBrowser().openAndWaitFor(BuildDetailsPage.class, dependentProject.getName(), buildNumber);
        getBrowser().waitForElement(WebUtils.toValidHtmlName(projectA.getName() + "-default-artifactA.jar"));

        // delete the artifact from the file system
        repository.clean();

        getBrowser().openAndWaitFor(BuildDetailsPage.class, dependentProject.getName(), buildNumber);
        assertFalse(getBrowser().isLinkPresent(projectA.getName() + "-default-artifactA.jar"));
    }

    public void testRetrieveCommandLogging() throws Exception
    {
        getBrowser().loginAsAdmin();

        DepAntProject projectA = projectConfigurations.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        insertProject(projectA);

        DepAntProject projectB = projectConfigurations.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact.jar");
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectA);
        rpcClient.RemoteApi.waitForBuildToComplete(projectB.getName(), 1);

        StageLogPage log = getBrowser().openAndWaitFor(StageLogPage.class, projectB.getName(), 1L, "default");
        assertTrue(log.isLogAvailable());

        if (getBrowser().isFirefox())
        {
            log.clickDownloadLink();
            // check for a reference to the artifact retrieval in the log.
            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    return getBrowser().getBodyText().contains(randomName + "A#artifact(1)");
                }
            }, 30000, "artifact retrieval to appear in log");
        }
    }

    public void testProjectDependenciesTab() throws Exception
    {
        DepAntProject projectA = projectConfigurations.createDepAntProject(randomName + "A");
        insertProject(projectA);

        DepAntProject projectB = projectConfigurations.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA).setTransitive(true);
        insertProject(projectB);

        DepAntProject projectC = projectConfigurations.createDepAntProject(randomName + "C");
        projectC.addDependency(projectB);
        insertProject(projectC);

        getBrowser().loginAsAdmin();
        ProjectDependenciesPage page = getBrowser().openAndWaitFor(ProjectDependenciesPage.class, projectB.getName());
        assertTrue(page.isUpstreamPresent(projectA.getName(), 0, 0));
        assertTrue(page.isUpstreamPresent(projectB.getName(), 1, 0));
        assertTrue(page.isDownstreamPresent(projectB.getName(), 0, 0));
        assertTrue(page.isDownstreamPresent(projectC.getName(), 1, 0));

        // Go to a project with transitive dependencies
        page = getBrowser().openAndWaitFor(ProjectDependenciesPage.class, projectC.getName());
        if (page.getTransitiveMode() != ProjectDependencyGraphBuilder.TransitiveMode.FULL)
        {
            page.setTransitiveModeAndWait(ProjectDependencyGraphBuilder.TransitiveMode.FULL);
            getBrowser().waitForElement(page.getUpstreamId(projectA.getName(), 0, 0));
        }

        assertTrue(page.isUpstreamPresent(projectA.getName(), 0, 0));
        assertTrue(page.isUpstreamPresent(projectB.getName(), 1, 0));
        
        // Filter out transients, make sure this takes effect.
        page.setTransitiveModeAndWait(ProjectDependencyGraphBuilder.TransitiveMode.NONE);

        getBrowser().waitForElement(page.getUpstreamId(projectB.getName(), 0, 0));
        assertFalse(page.isUpstreamPresent(projectA.getName(), 0, 0));
    }
}
