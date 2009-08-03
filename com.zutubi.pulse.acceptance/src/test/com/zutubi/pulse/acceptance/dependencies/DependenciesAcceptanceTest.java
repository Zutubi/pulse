package com.zutubi.pulse.acceptance.dependencies;

import static com.zutubi.pulse.core.dependency.ivy.IvyManager.*;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.SystemUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.List;

public class DependenciesAcceptanceTest extends BaseDependenciesAcceptanceTest
{
    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        repository = new Repository();
        repository.clear();
    }

    @Override
    protected void tearDown() throws Exception
    {
        logout();

        super.tearDown();
    }

    public void testPublish_NoArtifacts() throws Exception
    {
        // configure project.
        Project project = new Project(randomName());
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();

        int buildNumber = triggerSuccessfulBuild(project, build);

        // verify existance of expected artifacts.
        assertIvyInRepository(project, buildNumber);
    }

    public void testPublish_SingleArtifact() throws Exception
    {
        Project project = new Project(randomName());
        Artifact artifact = project.getDefaultRecipe().addArtifact("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifact);
    }

    public void testPublish_MultipleArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        List<Artifact> artifacts = project.addArtifacts("artifact.jar", "another-artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");
        build.addFileToCreate("build/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifacts.get(0));
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifacts.get(1));
    }

    public void testPublish_MultipleStages() throws Exception
    {
        Project project = new Project(randomName());
        Artifact artifact = project.addArtifact("artifact.jar");
        project.addStage("stage");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, project.getStage("default"), buildNumber, artifact);
        assertArtifactInRepository(project, project.getStage("stage"), buildNumber, artifact);
    }

    public void testPublishFails_MissingArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        Artifact artifact = project.addArtifact("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("incorrect/path/artifact.jar");

        int buildNumber = triggerBuild(project, build);
        assertEquals(ResultState.ERROR, getBuildStatus(project.getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(project, buildNumber);
        assertArtifactNotInRepository(project, project.getDefaultStage(), buildNumber, artifact);
    }

    public void testPublish_StatusConfiguration() throws Exception
    {
        Project project = new Project(randomName());
        project.setStatus(STATUS_RELEASE);
        project.addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_RELEASE, project, buildNumber);
    }

    public void testPublish_DefaultStatus() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyStatus(STATUS_INTEGRATION, project, buildNumber);
    }

    public void testStatusValidation() throws Exception
    {
        try
        {
            Project project = new Project(randomName());
            project.setStatus("invalid");
            project.addArtifacts("artifact.jar");
            createProject(project);
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("status is invalid"));
        }
    }

    public void testRemoteTriggerWithCustomStatus() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build, STATUS_MILESTONE);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_MILESTONE, project, buildNumber);
    }

    public void testRetrieve_SingleArtifact() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addExpectedFile("lib/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectB, build);

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testRetrieve_MultipleArtifacts() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("artifact.jar", "another-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFilesToCreate("build/artifact.jar", "build/another-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addExpectedFiles("lib/artifact.jar", "lib/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectB, buildB);

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testRetrieve_SpecificStage() throws Exception
    {
        // need different recipies that produce different artifacts.
        Project project = new Project(randomName());
        Artifact recipeAArtifact = project.addRecipe("recipeA").addArtifact("artifactA.jar");
        Artifact recipeBArtifact = project.addRecipe("recipeB").addArtifact("artifactB.jar");
        Stage stageA = project.addStage("A");
        stageA.setRecipe(project.getRecipe("recipeA"));
        Stage stageB = project.addStage("B");
        stageB.setRecipe(project.getRecipe("recipeB"));
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFilesToCreate("build/artifactA.jar", "build/artifactB.jar");
        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, stageA, buildNumber, recipeAArtifact);
        assertArtifactNotInRepository(project, stageB, buildNumber, recipeAArtifact);

        assertArtifactInRepository(project, stageB, buildNumber, recipeBArtifact);
        assertArtifactNotInRepository(project, stageA, buildNumber, recipeBArtifact);
    }

    public void testRetrieve_SpeicificRevision() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("default-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/default-artifact.jar");
        
        // build twice and then depend on the first.
        int buildNumber = triggerSuccessfulBuild(projectA, buildA);
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.setRetrievalPattern("lib/[artifact]-[revision].[ext]");
        projectB.addDependency(new Dependency(projectA, true, "default", "" + buildNumber));
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addExpectedFiles("lib/default-artifact-" + buildNumber + ".jar");
        triggerSuccessfulBuild(projectB, buildB);
    }

    public void testRetrieve_MultipleProjects() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addArtifacts("projectB-artifact.jar");
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addFileToCreate("build/projectB-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);

        Project projectC = new Project(randomName());
        projectC.addDependency(projectA);
        projectC.addDependency(projectB);
        createProject(projectC);

        AntBuildConfiguration buildC = new AntBuildConfiguration();
        buildC.addExpectedFiles("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectC, buildC);
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieve_TransitiveDependencies() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addArtifacts("projectB-artifact.jar");
        projectB.addDependency(new Dependency(projectA, true));
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addFileToCreate("build/projectB-artifact.jar");
        buildB.addExpectedFile("lib/projectA-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);

        Project projectC = new Project(randomName());
        projectC.addDependency(projectB);
        createProject(projectC);

        AntBuildConfiguration buildC = new AntBuildConfiguration();
        buildC.addExpectedFiles("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectC, buildC);
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieve_TransitiveDependenciesDisabled() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addArtifacts("projectB-artifact.jar");
        projectB.addDependency(projectA);
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addFileToCreate("build/projectB-artifact.jar");
        buildB.addExpectedFile("lib/projectA-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);

        Project projectC = new Project(randomName());
        projectC.addDependency(new Dependency(projectB, false));
        createProject(projectC);

        AntBuildConfiguration buildC = new AntBuildConfiguration();
        buildC.addExpectedFiles("lib/projectB-artifact.jar");
        buildC.addNotExpectedFile("lib/projectA-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectC, buildC);
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieveFails_MissingDependencies() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("artifact.jar");
        createProject(projectA);

        // do not build project a simulating dependency not available.

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addExpectedFile("lib/artifact.jar");

        int buildNumber = triggerBuild(projectB, buildB);
        assertEquals(ResultState.FAILURE, getBuildStatus(projectB.getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(projectB, buildNumber);
    }

    public void testDependentBuild_TriggeredOnSuccess() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("artifact.jar");
        createProject(projectA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);
    }

    public void testDependentBuild_PropagateStatus() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.setStatus(STATUS_RELEASE);
        createProject(projectA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        projectB.setStatus(STATUS_INTEGRATION);
        projectB.setPropagateStatus(true);
        createProject(projectB);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        assertIvyStatus(STATUS_RELEASE, projectB, 1);
        assertIvyRevision("1", projectB, "1");
    }

    public void testDependentBuild_PropagateVersion() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.setVersion("FIXED");
        createProject(projectA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        projectB.setPropagateVersion(true);
        createProject(projectB);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        assertIvyInRepository(projectB, "FIXED");
        assertIvyRevision("FIXED", projectB, "FIXED");
    }

    public void testRepositoryFormat_OrgSpecified() throws Exception
    {
        Project project = new Project(randomName(), "org");
        project.addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyInRepository(project, buildNumber);
    }

    public void testArtifactPattern() throws Exception
    {
        Project project = new Project(randomName());
        Artifact artifact = project.addArtifact("artifact-12345.jar");
        artifact.setArtifactPattern("(.+)-[0-9]+\\.(.+)");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact-12345.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // update the artifact details because the -1 should be filtered out by the artifact pattern.
        artifact.setName("artifact");
        artifact.setExtension("jar");
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifact);
    }

    public void testUnusualCharactersInArtifactName() throws Exception
    {
        // The criteria for artifact names is that they must be allowed in a URI.  This
        // is because the internal artifact repository is accessed by ivy via HTTP.
        
        String validCharacters = "!()._-";
        String invalidCharacters = "@#%^&";

        // $ is not allowed in an artifact name 

        runBuildWithCharacterInArtifactName(validCharacters, ResultState.SUCCESS);
        runBuildWithCharacterInArtifactName(invalidCharacters, ResultState.ERROR);
    }

    private void runBuildWithCharacterInArtifactName(String testCharacters, ResultState expected) throws Exception
    {
        for (char c : testCharacters.toCharArray())
        {
            Project project = new Project(randomName());
            Artifact artifact = project.addArtifact("artifact-" + c + ".jar");
            createProject(project);

            AntBuildConfiguration build = new AntBuildConfiguration();
            // The ant script on unix evals its arguments, so we need to escape
            // these characters lest the shell choke on them.
            String resolvedChar = SystemUtils.IS_WINDOWS ? Character.toString(c) : "\\" + c;
            build.addFileToCreate("build/artifact-" + resolvedChar + ".jar");

            int buildNumber = triggerBuild(project, build);
            assertEquals("Unexpected result for character: " + c, expected, getBuildStatus(project.getName(), buildNumber));

            if (expected == ResultState.SUCCESS)
            {
                assertIvyInRepository(project, buildNumber);
                assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifact);
            }
            else
            {
                assertIvyNotInRepository(project, buildNumber);
                assertArtifactNotInRepository(project, project.getDefaultStage(), buildNumber, artifact);
            }
        }
    }

    private void assertIvyStatus(String expectedStatus, Project project, int buildNumber) throws IOException
    {
        assertEquals(expectedStatus, repository.getIvyFile(project.getOrg(), project.getName(), buildNumber).getStatus());
    }

    private void assertIvyRevision(String expectedRevision, Project project, String version) throws IOException
    {
        assertEquals(expectedRevision, repository.getIvyFile(project.getOrg(), project.getName(), version).getRevision());
    }

    private void assertIvyInRepository(Project project, Object revision) throws IOException
    {
        assertInRepository(repository.getIvyFile(project.getOrg(), project.getName(), revision).getPath());
    }

    private void assertIvyNotInRepository(Project project, Object revision) throws IOException
    {
        assertNotInRepository(repository.getIvyFile(project.getOrg(), project.getName(), revision).getPath());
    }

    private void assertArtifactInRepository(Project project, Stage stage, Object revision, Artifact artifact) throws IOException
    {
        assertInRepository(repository.getArtifactFile(project.getOrg(), project.getName(), stage.getName(), revision, artifact.getName(), artifact.getExtension()).getPath());
    }

    private void assertArtifactNotInRepository(Project project, Stage stage, Object revision, Artifact artifact) throws IOException
    {
        assertNotInRepository(repository.getArtifactFile(project.getOrg(), project.getName(), stage.getName(), revision, artifact.getName(), artifact.getExtension()).getPath());
    }

    private void assertInRepository(String baseArtifactName) throws IOException
    {
        // all artifacts are being published with .md5 and .sha1 hashes.
        assertTrue(repository.waitUntilInRepository(baseArtifactName));
        assertTrue(repository.waitUntilInRepository(baseArtifactName + ".md5"));
        assertTrue(repository.waitUntilInRepository(baseArtifactName + ".sha1"));
    }

    public void assertNotInRepository(String baseArtifactName) throws IOException
    {
        assertFalse(repository.waitUntilInRepository(baseArtifactName));
    }

}
