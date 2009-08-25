package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.browse.ProjectDependenciesForm;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectDependenciesPage;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;

import static java.lang.String.valueOf;

public class DependenciesUIAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private SeleniumBrowser browser;
    private Repository repository;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        repository = new Repository();
        repository.clear();

        browser = new SeleniumBrowser();
        browser.start();
    }

    @Override
    protected void tearDown() throws Exception
    {
        browser.stop();
        
        logout();

        super.tearDown();
    }

    public void testBuildSummaryReport() throws Exception
    {
        browser.loginAsAdmin();

        String randomName = randomName();
        ProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName + "A");
        projectA.addArtifact("artifactA.jar");
        projectA.createProject();
        long projectABuildNumber = projectA.triggerSuccessfulBuild();

        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, projectA.getName(), projectABuildNumber);
        assertFalse(summaryPage.hasDependencies());

        ProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName + "B");
        projectB.addArtifact("artifactB.jar");
        projectB.createProject();
        long projectBBuildNumber = projectB.triggerSuccessfulBuild();

        ProjectHelper dependentProject = new DepAntProjectHelper(xmlRpcHelper, randomName + "C");
        dependentProject.addDependency(projectA);
        dependentProject.addDependency(projectB);
        dependentProject.createProject();
        long buildNumber = dependentProject.triggerSuccessfulBuild();

        summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, dependentProject.getName(), buildNumber);
        assertTrue(summaryPage.hasDependencies());
        
        BuildSummaryPage.DependencyRow row1 = summaryPage.getDependencyRow(1);
        BuildSummaryPage.DependencyRow row2 = summaryPage.getDependencyRow(2);

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

        String randomName = randomName();
        ProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName + "A");
        projectA.addArtifact("artifactA.jar");
        projectA.createProject();
        projectA.triggerSuccessfulBuild();

        ProjectHelper dependentProject = new DepAntProjectHelper(xmlRpcHelper, randomName + "C");
        dependentProject.addDependency(projectA);
        dependentProject.createProject();
        long buildNumber = dependentProject.triggerSuccessfulBuild();

        browser.openAndWaitFor(BuildSummaryPage.class, dependentProject.getName(), buildNumber);
        assertTrue(browser.isLinkPresent(projectA.getName() + "-default-artifactA.jar"));

        // delete the artifact from the file system
        repository.clear();

        browser.openAndWaitFor(BuildSummaryPage.class, dependentProject.getName(), buildNumber);
        assertFalse(browser.isLinkPresent(projectA.getName() + "-default-artifactA.jar"));
    }

    public void testProjectDependenciesTab() throws Exception
    {
        String randomName = randomName();
        ProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName + "A");
        projectA.createProject();

        ProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName + "B");
        projectB.addDependency(new DependencyHelper(projectA, true));
        projectB.createProject();

        ProjectHelper projectC = new DepAntProjectHelper(xmlRpcHelper, randomName + "C");
        projectC.addDependency(projectB);
        projectC.createProject();

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
            ProjectDependenciesForm form = browser.createForm(ProjectDependenciesForm.class);
            form.submitMode(ProjectDependencyGraphBuilder.TransitiveMode.FULL);
            browser.waitForElement(page.getUpstreamId(projectA.getName(), 0, 0));
        }

        assertTrue(page.isUpstreamPresent(projectA.getName(), 0, 0));
        assertTrue(page.isUpstreamPresent(projectB.getName(), 1, 0));
        
        // Filter out transients, make sure this takes effect.
        ProjectDependenciesForm form = browser.createForm(ProjectDependenciesForm.class);
        assertTrue(form.isFormPresent());
        form.submitMode(ProjectDependencyGraphBuilder.TransitiveMode.NONE);

        browser.waitForElement(page.getUpstreamId(projectB.getName(), 0, 0));
        assertFalse(page.isUpstreamPresent(projectA.getName(), 0, 0));
    }
}
