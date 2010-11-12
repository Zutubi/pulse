package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildDetailsPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectDependenciesPage;
import com.zutubi.pulse.acceptance.pages.browse.StageLogPage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.util.Condition;

import static java.lang.String.valueOf;

public class DependenciesUIAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private SeleniumBrowser browser;
    private Repository repository;
    private String randomName;
    private BuildRunner buildRunner;
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private SeleniumBrowserFactory browserFactory;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        randomName = randomName();

        repository = new Repository();
        repository.clean();

        browserFactory = new DefaultSeleniumBrowserFactory();
        browser = browserFactory.newBrowser();

        buildRunner = new BuildRunner(xmlRpcHelper);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);
    }

    @Override
    protected void tearDown() throws Exception
    {
        browserFactory.stop();
        
        logout();

        super.tearDown();
    }

    private void insertProject(ProjectConfigurationHelper project) throws Exception
    {
        configurationHelper.insertProject(project.getConfig(), false);
    }

    public void testBuildDetailsReport() throws Exception
    {
        browser.loginAsAdmin();

        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifactA.jar");
        projectA.addFilesToCreate("build/artifactA.jar");
        insertProject(projectA);
        long projectABuildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        BuildDetailsPage detailsPage = browser.openAndWaitFor(BuildDetailsPage.class, projectA.getName(), projectABuildNumber);
        assertFalse(detailsPage.isDependenciesTablePresent());

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addArtifacts("build/artifactB.jar");
        projectB.addFilesToCreate("build/artifactB.jar");
        insertProject(projectB);
        long projectBBuildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        DepAntProject dependentProject = projects.createDepAntProject(randomName + "C");
        dependentProject.addDependency(projectA);
        dependentProject.addDependency(projectB);
        insertProject(dependentProject);
        long buildNumber = buildRunner.triggerSuccessfulBuild(dependentProject.getConfig());

        detailsPage = browser.openAndWaitFor(BuildDetailsPage.class, dependentProject.getName(), buildNumber);
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
        browser.loginAsAdmin();

        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifactA.jar");
        projectA.addFilesToCreate("build/artifactA.jar");
        insertProject(projectA);
        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject dependentProject = projects.createDepAntProject(randomName + "C");
        dependentProject.addDependency(projectA);
        insertProject(dependentProject);
        long buildNumber = buildRunner.triggerSuccessfulBuild(dependentProject.getConfig());

        browser.openAndWaitFor(BuildDetailsPage.class, dependentProject.getName(), buildNumber);
        assertTrue(browser.isLinkPresent(projectA.getName() + "-default-artifactA.jar"));

        // delete the artifact from the file system
        repository.clean();

        browser.openAndWaitFor(BuildDetailsPage.class, dependentProject.getName(), buildNumber);
        assertFalse(browser.isLinkPresent(projectA.getName() + "-default-artifactA.jar"));
    }

    public void testRetrieveCommandLogging() throws Exception
    {
        browser.loginAsAdmin();

        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact.jar");
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectA);
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        StageLogPage log = browser.openAndWaitFor(StageLogPage.class, projectB.getName(), 1L, "default");
        assertTrue(log.isLogAvailable());

        if (browser.isFirefox())
        {
            log.clickDownloadLink();
            // check for a reference to the artifact retrieval in the log.
            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    return browser.getBodyText().contains(randomName + "A#artifact(1)");
                }
            }, 30000, "artifact retrieval to appear in log");
        }
    }

    public void testProjectDependenciesTab() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA).setTransitive(true);
        insertProject(projectB);

        DepAntProject projectC = projects.createDepAntProject(randomName + "C");
        projectC.addDependency(projectB);
        insertProject(projectC);

        browser.loginAsAdmin();
        ProjectDependenciesPage page = browser.openAndWaitFor(ProjectDependenciesPage.class, projectB.getName());
        assertTrue(page.isUpstreamPresent(projectA.getName(), 0, 0));
        assertTrue(page.isUpstreamPresent(projectB.getName(), 1, 0));
        assertTrue(page.isDownstreamPresent(projectB.getName(), 0, 0));
        assertTrue(page.isDownstreamPresent(projectC.getName(), 1, 0));

        // Go to a project with transitive dependencies
        page = browser.openAndWaitFor(ProjectDependenciesPage.class, projectC.getName());
        if (page.getTransitiveMode() != ProjectDependencyGraphBuilder.TransitiveMode.FULL)
        {
            page.setTransitiveModeAndWait(ProjectDependencyGraphBuilder.TransitiveMode.FULL);
            browser.waitForElement(page.getUpstreamId(projectA.getName(), 0, 0));
        }

        assertTrue(page.isUpstreamPresent(projectA.getName(), 0, 0));
        assertTrue(page.isUpstreamPresent(projectB.getName(), 1, 0));
        
        // Filter out transients, make sure this takes effect.
        page.setTransitiveModeAndWait(ProjectDependencyGraphBuilder.TransitiveMode.NONE);

        browser.waitForElement(page.getUpstreamId(projectB.getName(), 0, 0));
        assertFalse(page.isUpstreamPresent(projectA.getName(), 0, 0));
    }
}
