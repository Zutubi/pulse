package com.zutubi.pulse.acceptance.dependencies;

import static com.zutubi.pulse.core.dependency.ivy.IvyManager.*;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.CollectionUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.List;

public class DependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private Repository repository;

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
        ProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        project.createProject();

        int buildNumber = project.triggerSuccessfulBuild();

        // verify existance of expected artifacts.
        assertIvyInRepository(project, buildNumber);
    }

    public void testPublish_SingleArtifact() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        ArtifactHelper artifact = project.getDefaultRecipe().addArtifact("artifact.jar");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");
        int buildNumber = project.triggerSuccessfulBuild();

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifact);
    }

    public void testPublish_MultipleArtifacts() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        List<ArtifactHelper> artifacts = project.addArtifacts("artifact.jar", "another-artifact.jar");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");
        project.addFileToCreate("build/another-artifact.jar");

        int buildNumber = project.triggerSuccessfulBuild();

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifacts.get(0));
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifacts.get(1));
    }

    public void testPublish_MultipleStages() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        ArtifactHelper artifact = project.addArtifact("artifact.jar");
        project.addStage("stage");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");

        int buildNumber = project.triggerSuccessfulBuild();

        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, project.getStage("default"), buildNumber, artifact);
        assertArtifactInRepository(project, project.getStage("stage"), buildNumber, artifact);
    }

    public void testPublishFails_MissingArtifacts() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        ArtifactHelper artifact = project.addArtifact("artifact.jar");
        project.createProject();

        project.addFileToCreate("incorrect/path/artifact.jar");

        int buildNumber = project.triggerCompleteBuild();
        assertEquals(ResultState.ERROR, getBuildStatus(project.getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(project, buildNumber);
        assertArtifactNotInRepository(project, project.getDefaultStage(), buildNumber, artifact);
    }

    public void testPublish_StatusConfiguration() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        project.setStatus(STATUS_RELEASE);
        project.addArtifacts("artifact.jar");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");

        int buildNumber = project.triggerSuccessfulBuild();

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_RELEASE, project, buildNumber);
    }

    public void testPublish_DefaultStatus() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        project.addArtifacts("artifact.jar");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");

        int buildNumber = project.triggerSuccessfulBuild();

        assertIvyStatus(STATUS_INTEGRATION, project, buildNumber);
    }

    public void testStatusValidation() throws Exception
    {
        try
        {
            DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
            project.setStatus("invalid");
            project.addArtifacts("artifact.jar");
            project.createProject();
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("status is invalid"));
        }
    }

    public void testRemoteTriggerWithCustomStatus() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        project.addArtifacts("artifact.jar");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");

        int buildNumber = project.triggerSuccessfulBuild(CollectionUtils.asPair("status", (Object)STATUS_MILESTONE));

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_MILESTONE, project, buildNumber);
    }

    public void testRetrieve_SingleArtifact() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.createProject();

        projectA.addFileToCreate("build/artifact.jar");
        projectA.triggerSuccessfulBuild();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addDependency(projectA);
        projectB.createProject();

        projectB.addExpectedFile("lib/artifact.jar");

        int buildNumber = projectB.triggerSuccessfulBuild();

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testRetrieve_MultipleArtifacts() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("artifact.jar", "another-artifact.jar");
        projectA.createProject();

        projectA.addFilesToCreate("build/artifact.jar", "build/another-artifact.jar");
        projectA.triggerSuccessfulBuild();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addDependency(projectA);
        projectB.createProject();

        projectB.addExpectedFiles("lib/artifact.jar", "lib/another-artifact.jar");

        int buildNumber = projectB.triggerSuccessfulBuild();

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testRetrieve_SpecificStage() throws Exception
    {
        // need different recipies that produce different artifacts.
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        ArtifactHelper recipeAArtifact = project.addRecipe("recipeA").addArtifact("artifactA.jar");
        ArtifactHelper recipeBArtifact = project.addRecipe("recipeB").addArtifact("artifactB.jar");
        StageHelper stageA = project.addStage("A");
        stageA.setRecipe(project.getRecipe("recipeA"));
        StageHelper stageB = project.addStage("B");
        stageB.setRecipe(project.getRecipe("recipeB"));
        project.createProject();

        project.addFilesToCreate("build/artifactA.jar", "build/artifactB.jar");
        int buildNumber = project.triggerSuccessfulBuild();

        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, stageA, buildNumber, recipeAArtifact);
        assertArtifactNotInRepository(project, stageB, buildNumber, recipeAArtifact);

        assertArtifactInRepository(project, stageB, buildNumber, recipeBArtifact);
        assertArtifactNotInRepository(project, stageA, buildNumber, recipeBArtifact);
    }

    public void testRetrieve_SpeicificRevision() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("default-artifact.jar");
        projectA.createProject();

        projectA.addFileToCreate("build/default-artifact.jar");
        
        // build twice and then depend on the first.
        int buildNumber = projectA.triggerSuccessfulBuild();
        projectA.triggerSuccessfulBuild();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.setRetrievalPattern("lib/[artifact]-[revision].[ext]");
        projectB.addDependency(new DependencyHelper(projectA, true, "default", "" + buildNumber));
        projectB.createProject();

        projectB.addExpectedFiles("lib/default-artifact-" + buildNumber + ".jar");
        projectB.triggerSuccessfulBuild();
    }

    public void testRetrieve_MultipleProjects() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("projectA-artifact.jar");
        projectA.createProject();

        projectA.addFileToCreate("build/projectA-artifact.jar");
        projectA.triggerSuccessfulBuild();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addArtifacts("projectB-artifact.jar");
        projectB.createProject();

        projectB.addFileToCreate("build/projectB-artifact.jar");
        projectB.triggerSuccessfulBuild();

        DepAntProjectHelper projectC = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectC.addDependency(projectA);
        projectC.addDependency(projectB);
        projectC.createProject();

        projectB.addExpectedFiles("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");

        int buildNumber = projectC.triggerSuccessfulBuild();
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieve_TransitiveDependencies() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("projectA-artifact.jar");
        projectA.createProject();

        projectA.addFileToCreate("build/projectA-artifact.jar");
        projectA.triggerSuccessfulBuild();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addArtifacts("projectB-artifact.jar");
        projectB.addDependency(new DependencyHelper(projectA, true));
        projectB.createProject();

        projectB.addFileToCreate("build/projectB-artifact.jar");
        projectB.addExpectedFile("lib/projectA-artifact.jar");
        projectB.triggerSuccessfulBuild();

        DepAntProjectHelper projectC = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectC.addDependency(projectB);
        projectC.createProject();

        projectC.addExpectedFiles("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");

        int buildNumber = projectC.triggerSuccessfulBuild();
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieve_TransitiveDependenciesDisabled() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("projectA-artifact.jar");
        projectA.createProject();

        projectA.addFileToCreate("build/projectA-artifact.jar");
        projectA.triggerSuccessfulBuild();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addArtifacts("projectB-artifact.jar");
        projectB.addDependency(projectA);
        projectB.createProject();

        projectB.addFileToCreate("build/projectB-artifact.jar");
        projectB.addExpectedFile("lib/projectA-artifact.jar");
        projectB.triggerSuccessfulBuild();

        DepAntProjectHelper projectC = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectC.addDependency(new DependencyHelper(projectB, false));
        projectC.createProject();

        projectC.addExpectedFiles("lib/projectB-artifact.jar");
        projectC.addNotExpectedFile("lib/projectA-artifact.jar");

        int buildNumber = projectC.triggerSuccessfulBuild();
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieveFails_MissingDependencies() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.createProject();

        // do not build projectA simulating dependency not available.

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addDependency(projectA);
        projectB.createProject();

        projectB.addExpectedFile("lib/artifact.jar");

        int buildNumber = projectB.triggerCompleteBuild();
        assertEquals(ResultState.FAILURE, getBuildStatus(projectB.getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(projectB, buildNumber);
    }

    public void testDependentBuild_TriggeredOnSuccess() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.createProject();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addDependency(projectA);
        projectB.createProject();

        projectA.addFileToCreate("build/artifact.jar");
        projectA.triggerSuccessfulBuild();

        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);
    }

    public void testDependentBuild_PropagateStatus() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.setStatus(STATUS_RELEASE);
        projectA.createProject();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addDependency(projectA);
        projectB.setStatus(STATUS_INTEGRATION);
        projectB.setPropagateStatus(true);
        projectB.createProject();

        projectA.addFileToCreate("build/artifact.jar");
        projectA.triggerSuccessfulBuild();

        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        assertIvyStatus(STATUS_RELEASE, projectB, 1);
        assertIvyRevision("1", projectB, "1");
    }

    public void testDependentBuild_PropagateVersion() throws Exception
    {
        DepAntProjectHelper projectA = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectA.addArtifacts("artifact.jar");
        projectA.setVersion("FIXED");
        projectA.createProject();

        DepAntProjectHelper projectB = new DepAntProjectHelper(xmlRpcHelper, randomName());
        projectB.addDependency(projectA);
        projectB.setPropagateVersion(true);
        projectB.createProject();

        projectA.addFileToCreate("build/artifact.jar");
        projectA.triggerSuccessfulBuild();

        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        assertIvyInRepository(projectB, "FIXED");
        assertIvyRevision("FIXED", projectB, "FIXED");
    }

    public void testRepositoryFormat_OrgSpecified() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName(), "org");
        project.addArtifacts("artifact.jar");
        project.createProject();

        project.addFileToCreate("build/artifact.jar");

        int buildNumber = project.triggerSuccessfulBuild();

        assertIvyInRepository(project, buildNumber);
    }

    public void testArtifactPattern() throws Exception
    {
        DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
        ArtifactHelper artifact = project.addArtifact("artifact-12345.jar");
        artifact.setArtifactPattern("(.+)-[0-9]+\\.(.+)");
        project.createProject();

        project.addFileToCreate("build/artifact-12345.jar");

        int buildNumber = project.triggerSuccessfulBuild();

        // update the artifact details because the '-12345' should be filtered out by the artifact pattern.
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
            DepAntProjectHelper project = new DepAntProjectHelper(xmlRpcHelper, randomName());
            ArtifactHelper artifact = project.addArtifact("artifact-" + c + ".jar");
            project.createProject();

            // The ant script on unix evals its arguments, so we need to escape
            // these characters lest the shell choke on them.
            String resolvedChar = SystemUtils.IS_WINDOWS ? Character.toString(c) : "\\" + c;
            project.addFileToCreate("build/artifact-" + resolvedChar + ".jar");

            int buildNumber = project.triggerCompleteBuild();
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

    private void assertIvyStatus(String expectedStatus, ProjectHelper project, int buildNumber) throws IOException
    {
        assertEquals(expectedStatus, repository.getIvyFile(project.getOrg(), project.getName(), buildNumber).getStatus());
    }

    private void assertIvyRevision(String expectedRevision, ProjectHelper project, String version) throws IOException
    {
        assertEquals(expectedRevision, repository.getIvyFile(project.getOrg(), project.getName(), version).getRevision());
    }

    private void assertIvyInRepository(ProjectHelper project, Object revision) throws IOException
    {
        assertInRepository(repository.getIvyFile(project.getOrg(), project.getName(), revision).getPath());
    }

    private void assertIvyNotInRepository(ProjectHelper project, Object revision) throws IOException
    {
        assertNotInRepository(repository.getIvyFile(project.getOrg(), project.getName(), revision).getPath());
    }

    private void assertArtifactInRepository(ProjectHelper project, StageHelper stage, Object revision, ArtifactHelper artifact) throws IOException
    {
        assertInRepository(repository.getArtifactFile(project.getOrg(), project.getName(), stage.getName(), revision, artifact.getName(), artifact.getExtension()).getPath());
    }

    private void assertArtifactNotInRepository(ProjectHelper project, StageHelper stage, Object revision, ArtifactHelper artifact) throws IOException
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
