package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.browse.ProjectDependenciesForm;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectDependenciesPage;
import com.zutubi.pulse.master.tove.config.user.DependencyTransitiveMode;

import static java.lang.String.valueOf;

public class DependenciesUIAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private SeleniumBrowser browser;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        Repository repository = new Repository();
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
        Project projectA = new DepAntProject(xmlRpcHelper, randomName + "A");
        projectA.addArtifact("artifactA.jar");
        projectA.createProject();
        long projectABuildNumber = projectA.triggerSuccessfulBuild();

        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, projectA.getName(), projectABuildNumber);
        assertFalse(summaryPage.hasDependencies());

        Project projectB = new DepAntProject(xmlRpcHelper, randomName + "B");
        projectB.addArtifact("artifactB.jar");
        projectB.createProject();
        long projectBBuildNumber = projectB.triggerSuccessfulBuild();

        Project dependentProject = new DepAntProject(xmlRpcHelper, randomName + "C");
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

    public void testProjectDependenciesTab() throws Exception
    {
        Project projectA = new DepAntProject(xmlRpcHelper, randomName());
        projectA.createProject();

        Project projectB = new DepAntProject(xmlRpcHelper, randomName());
        projectB.addDependency(new Dependency(projectA, true));
        projectB.createProject();

        Project projectC = new DepAntProject(xmlRpcHelper, randomName());
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
        assertTrue(page.isUpstreamPresent(projectA.getName(), 0, 0));
        assertTrue(page.isUpstreamPresent(projectB.getName(), 1, 0));
        
        // Filter out transients, make sure this takes effect.
        ProjectDependenciesForm form = browser.createForm(ProjectDependenciesForm.class);
        assertTrue(form.isFormPresent());
        form.submitMode(DependencyTransitiveMode.SHOW_DIRECT_DEPENDENCIES_ONLY);

        browser.waitForElement(page.getUpstreamId(projectB.getName(), 0, 0));
        assertFalse(page.isUpstreamPresent(projectA.getName(), 0, 0));
    }
}
