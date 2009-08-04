package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.SeleniumBrowser;

import static java.lang.String.valueOf;

public class DependenciesUIAcceptanceTest extends BaseDependenciesAcceptanceTest
{
    private SeleniumBrowser browser;

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
        Project projectA = new Project(randomName + "A");
        projectA.addArtifact("artifactA.jar");
        createProject(projectA);
        long projectABuildNumber = triggerSuccessfulBuild(projectA);

        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, projectA.getName(), projectABuildNumber);
        assertFalse(summaryPage.hasDependencies());

        Project projectB = new Project(randomName + "B");
        projectB.addArtifact("artifactB.jar");
        createProject(projectB);
        long projectBBuildNumber = triggerSuccessfulBuild(projectB);

        Project dependentProject = new Project(randomName + "C");
        dependentProject.addDependency(projectA);
        dependentProject.addDependency(projectB);
        createProject(dependentProject);
        long buildNumber = triggerSuccessfulBuild(dependentProject);

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


}
