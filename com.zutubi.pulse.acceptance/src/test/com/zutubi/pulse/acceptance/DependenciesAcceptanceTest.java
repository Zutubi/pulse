package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.util.*;
import static com.zutubi.util.Constants.MEGABYTE;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.Constants.TRIVIAL_ANT_REPOSITORY;
import static com.zutubi.pulse.core.dependency.ivy.IvyLatestRevisionMatcher.LATEST;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.*;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEPENDENCY_TRIGGER;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_RECIPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.tmatesoft.svn.core.SVNException;

public class DependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private Repository repository;
    private String randomName;
    private BuildRunner buildRunner;
    private ProjectConfigurations projects;
    private ConfigurationHelper configurationHelper;

    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        repository = new Repository();
        repository.clean();

        randomName = randomName();

        buildRunner = new BuildRunner(xmlRpcHelper);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);

        tmp = FileSystemUtils.createTempDir();
    }

    @Override
    protected void tearDown() throws Exception
    {
        logout();

        removeDirectory(tmp);

        super.tearDown();
    }

    private void insertProject(ProjectConfigurationHelper project) throws Exception
    {
        configurationHelper.insertProject(project.getConfig());
    }

    private void updateProject(ProjectConfigurationHelper project) throws Exception
    {
        configurationHelper.update(project.getConfig());
    }

    public void testPublish_NoArtifacts() throws Exception
    {
        // configure project.
        DepAntProject project = projects.createDepAntProject(randomName);
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        assertIvyInRepository(project, buildNumber);
    }

    public void testPublish_SingleArtifact() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact.jar");
        project.addFilesToCreate("build/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, "default", buildNumber, "artifact", "jar");
    }

    public void testPublish_MultipleArtifacts() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact.jar", "build/another-artifact.jar");
        project.addFilesToCreate("build/artifact.jar", "build/another-artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, "default", buildNumber, "artifact", "jar");
        assertArtifactInRepository(project, "default", buildNumber, "another-artifact", "jar");
    }

    public void testPublish_MultipleStages() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact.jar");
        project.addStage("stage");
        project.addFilesToCreate("build/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, "default", buildNumber, "artifact", "jar");
        assertArtifactInRepository(project, "stage", buildNumber, "artifact", "jar");
    }

    public void testPublishFails_MissingArtifacts() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact.jar");
        project.addFilesToCreate("incorrect/path/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerAndWaitForBuild(project.getConfig());
        assertEquals(ResultState.ERROR, buildRunner.getBuildStatus(project.getConfig(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(project, buildNumber);
        assertArtifactNotInRepository(project, "default", buildNumber, "artifact", "jar");
    }

    public void testPublish_StatusConfiguration() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.getConfig().getDependencies().setStatus(STATUS_RELEASE);
        project.addArtifacts("build/artifact.jar");
        project.addFilesToCreate("build/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_RELEASE, project, buildNumber);
    }

    public void testPublish_DefaultStatus() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact.jar");
        project.addFilesToCreate("build/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_INTEGRATION, project, buildNumber);
    }

    public void testStatusValidation() throws Exception
    {
        try
        {
            DepAntProject project = projects.createDepAntProject(randomName);
            project.getConfig().getDependencies().setStatus("invalid");
            project.addArtifacts("build/artifact.jar");
            insertProject(project);
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("status is invalid"));
        }
    }

    public void testRemoteTriggerWithCustomStatus() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact.jar");
        project.addFilesToCreate("build/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig(), CollectionUtils.asPair("status", (Object) STATUS_MILESTONE));

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project, buildNumber);
        assertIvyStatus(STATUS_MILESTONE, project, buildNumber);
    }

    public void testRetrieve_SingleArtifact() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        insertProject(projectA);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        assertIvyInRepository(projectA, buildNumber);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact.jar");
        insertProject(projectB);

        buildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testRetrieve_MultipleArtifacts() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar", "build/another-artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar", "build/another-artifact.jar");
        insertProject(projectA);

        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact.jar", "lib/another-artifact.jar");
        insertProject(projectB);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testRetrieve_SpecificStage() throws Exception
    {
        // need different recipes that produce different artifacts.
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addRecipe("recipeA").addArtifacts("build/artifactA.jar");
        project.addRecipe("recipeB").addArtifacts("build/artifactB.jar");
        project.addStage("A").setRecipe("recipeA");
        project.addStage("B").setRecipe("recipeB");
        project.addFilesToCreate("build/artifactA.jar", "build/artifactB.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        assertIvyInRepository(project, buildNumber);
        assertArtifactInRepository(project, "A", buildNumber, "artifactA", "jar");
        assertArtifactNotInRepository(project, "B", buildNumber, "artifactA", "jar");

        assertArtifactInRepository(project, "B", buildNumber, "artifactB", "jar");
        assertArtifactNotInRepository(project, "A", buildNumber, "artifactB", "jar");
    }

    public void testRetrieve_CorrespondingStages() throws Exception
    {
        DepAntProject upstreamProject = projects.createDepAntProject(randomName + "-upstream", false);
        upstreamProject.addRecipe("recipeA").addArtifacts("build/artifactA.jar");
        upstreamProject.addRecipe("recipeB").addArtifacts("build/artifactB.jar");
        upstreamProject.addStage("Stage A").setRecipe("recipeA");
        upstreamProject.addStage("Stage B").setRecipe("recipeB");
        upstreamProject.addFilesToCreate("build/artifactA.jar", "build/artifactB.jar");
        insertProject(upstreamProject);

        buildRunner.triggerSuccessfulBuild(upstreamProject.getConfig());

        DepAntProject downstreamProject = projects.createDepAntProject(randomName + "-downstream", false);
        DependencyConfiguration dependencyConfig = downstreamProject.addDependency(upstreamProject.getConfig());
        dependencyConfig.setStageType(DependencyConfiguration.StageType.CORRESPONDING_STAGES);

        BuildStageConfiguration stageA = downstreamProject.addStage("Stage A");
        downstreamProject.addStageProperty(stageA, DepAntProject.PROPERTY_EXPECTED_LIST, "lib/artifactA.jar");
        downstreamProject.addStageProperty(stageA, DepAntProject.PROPERTY_NOT_EXPECTED_LIST, "lib/artifactB.jar");
        BuildStageConfiguration stageC = downstreamProject.addStage("Stage C");
        downstreamProject.addStageProperty(stageC, DepAntProject.PROPERTY_NOT_EXPECTED_LIST, "list/artifactA.jar,lib/artifactB.jar");
        insertProject(downstreamProject);

        buildRunner.triggerSuccessfulBuild(downstreamProject.getConfig());
    }

    public void testRetrieve_CorrespondingStagesIvyCaching() throws Exception
    {
        final String RECIPE = "recipe";
        final String ARTIFACT = "art.txt";
        final String STAGE_A = "Stage A";
        final String STAGE_B = "Stage B";

        DepAntProject upstreamProject = projects.createDepAntProject(randomName + "-upstream", false);
        upstreamProject.addRecipe(RECIPE).addArtifacts(ARTIFACT);
        upstreamProject.addStage(STAGE_A).setRecipe(RECIPE);
        upstreamProject.addStage(STAGE_B).setRecipe(RECIPE);
        upstreamProject.addFilesToCreate(ARTIFACT);
        insertProject(upstreamProject);

        buildRunner.triggerSuccessfulBuild(upstreamProject.getConfig());

        AgentConfiguration masterAgent = configurationHelper.getAgentReference(AgentManager.MASTER_AGENT_NAME);
        DepAntProject downstreamProject = projects.createDepAntProject(randomName + "-downstream", false);
        DependencyConfiguration dependencyConfig = downstreamProject.addDependency(upstreamProject.getConfig());
        dependencyConfig.setStageType(DependencyConfiguration.StageType.CORRESPONDING_STAGES);
        downstreamProject.addRecipe(RECIPE).addArtifacts("lib/" + ARTIFACT);
        BuildStageConfiguration downstreamStageA = downstreamProject.addStage(STAGE_A);
        downstreamStageA.setRecipe(RECIPE);
        downstreamStageA.setAgent(masterAgent);
        BuildStageConfiguration downstreamStageB = downstreamProject.addStage(STAGE_B);
        downstreamStageB.setAgent(masterAgent);
        downstreamStageB.setRecipe(RECIPE);
        insertProject(downstreamProject);

        int buildNumber = buildRunner.triggerSuccessfulBuild(downstreamProject.getConfig());

        checkCorrespondingStageFile(downstreamProject, STAGE_A, buildNumber);
        checkCorrespondingStageFile(downstreamProject, STAGE_B, buildNumber);
    }

    private void checkCorrespondingStageFile(DepAntProject project, String stage, int buildNumber) throws IOException
    {
        String path = repository.getArtifactPath(project.getConfig().getOrganisation(), project.getConfig().getName(), stage, Integer.toString(buildNumber), "art", "txt");
        File aFile = new File(repository.getBase(), path);
        assertThat(IOUtils.fileToString(aFile), containsString("PULSE_STAGE = " + stage));
    }

    public void testRetrieve_SpecificRevision() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/default-artifact.jar");
        projectA.addFilesToCreate("build/default-artifact.jar");
        insertProject(projectA);

        // build twice and then depend on the first.
        int buildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());
        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.getConfig().getDependencies().setRetrievalPattern("lib/[artifact]-[revision].[ext]");

        DependencyConfiguration dependencyConfig = projectB.addDependency(projectA.getConfig());
        dependencyConfig.setRevision(DependencyConfiguration.REVISION_CUSTOM);
        dependencyConfig.setTransitive(true);
        dependencyConfig.setCustomRevision(String.valueOf(buildNumber));

        projectB.addExpectedFiles("lib/default-artifact-" + buildNumber + ".jar");
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectB.getConfig());
    }

    public void testRetrieve_MultipleProjects() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/projectA-artifact.jar");
        projectA.addFilesToCreate("build/projectA-artifact.jar");
        insertProject(projectA);
        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addArtifacts("build/projectB-artifact.jar");
        projectB.addFilesToCreate("build/projectB-artifact.jar");
        insertProject(projectB);
        buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        DepAntProject projectC = projects.createDepAntProject(randomName + "C");
        projectC.addDependency(projectA.getConfig());
        projectC.addDependency(projectB.getConfig());
        projectC.addExpectedFiles("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");
        insertProject(projectC);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectC.getConfig());
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieve_TransitiveDependencies() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/projectA-artifact.jar");
        projectA.addFilesToCreate("build/projectA-artifact.jar");
        insertProject(projectA);
        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addArtifacts("build/projectB-artifact.jar");
        projectB.addDependency(projectA.getConfig()).setTransitive(true);
        projectB.addFilesToCreate("build/projectB-artifact.jar");
        projectB.addExpectedFiles("lib/projectA-artifact.jar");
        insertProject(projectB);
        buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        DepAntProject projectC = projects.createDepAntProject(randomName + "C");
        projectC.addDependency(projectB.getConfig());
        projectC.addExpectedFiles("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");
        insertProject(projectC);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectC.getConfig());
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieve_TransitiveDependenciesDisabled() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/projectA-artifact.jar");
        projectA.addFilesToCreate("build/projectA-artifact.jar");
        insertProject(projectA);
        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addArtifacts("build/projectB-artifact.jar");
        projectB.addDependency(projectA.getConfig());
        projectB.addFilesToCreate("build/projectB-artifact.jar");
        projectB.addExpectedFiles("lib/projectA-artifact.jar");
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        DepAntProject projectC = projects.createDepAntProject(randomName + "C");
        projectC.addDependency(projectB.getConfig()).setTransitive(false);
        projectC.addExpectedFiles("lib/projectB-artifact.jar");
        projectC.addNotExpectedFiles("lib/projectA-artifact.jar");
        insertProject(projectC);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectC.getConfig());
        assertIvyInRepository(projectC, buildNumber);
    }

    public void testRetrieveFails_MissingDependencies() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        insertProject(projectA);

        // do not build projectA simulating dependency not available.

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact.jar");
        insertProject(projectB);

        int buildNumber = buildRunner.triggerAndWaitForBuild(projectB.getConfig());
        assertEquals(ResultState.FAILURE, xmlRpcHelper.getBuildStatus(projectB.getConfig().getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(projectB, buildNumber);
    }

    public void testDependentBuild_TriggeredOnSuccess() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        xmlRpcHelper.waitForBuildToComplete(projectB.getConfig().getName(), 1);
    }

    public void testDependentBuildReason() throws Exception
    {
        DepAntProject upstream = projects.createDepAntProject(randomName + "-upstream");
        insertProject(upstream);

        DepAntProject downstream = projects.createDepAntProject(randomName + "-downstream");
        downstream.addDependency(upstream);
        insertProject(downstream);

        buildRunner.triggerSuccessfulBuild(upstream);
        xmlRpcHelper.waitForBuildToComplete(downstream.getConfig().getName(), 1);

        // verify that the build reasons are as expected.
        assertEquals("trigger via remote api by admin", xmlRpcHelper.getBuildReason(upstream.getConfig().getName(), 1));
        assertEquals("dependent of " + upstream.getConfig().getName(), xmlRpcHelper.getBuildReason(downstream.getConfig().getName(), 1));
    }

    public void testRebuildBuildReason() throws Exception
    {
        DepAntProject upstream = projects.createDepAntProject(randomName + "-upstream");
        insertProject(upstream);

        DepAntProject downstream = projects.createDepAntProject(randomName + "-downstream");
        downstream.addDependency(upstream);
        insertProject(downstream);

        buildRunner.triggerRebuild(downstream);
        xmlRpcHelper.waitForBuildToComplete(downstream.getConfig().getName(), 1);

        // verify that the build reasons are as expected.
        assertEquals("build with dependencies of " + downstream.getConfig().getName(), xmlRpcHelper.getBuildReason(upstream.getConfig().getName(), 1));
        assertEquals("trigger via remote api by admin", xmlRpcHelper.getBuildReason(downstream.getConfig().getName(), 1));
    }

    public void testDependentBuild_PropagateStatus() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.getConfig().getDependencies().setStatus(STATUS_RELEASE);
        projectA.addFilesToCreate("build/artifact.jar");
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.getConfig().getDependencies().setStatus(STATUS_INTEGRATION);

        DependentBuildTriggerConfiguration trigger = projectB.getTrigger(DEPENDENCY_TRIGGER);
        trigger.setPropagateStatus(true);

        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        xmlRpcHelper.waitForBuildToComplete(projectB.getConfig().getName(), 1);

        assertIvyStatus(STATUS_RELEASE, projectB, 1);
        assertIvyRevision("1", projectB, "1");
    }

    public void testDependentBuild_PropagateVersion() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        projectA.getConfig().getDependencies().setVersion("FIXED");
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());

        DependentBuildTriggerConfiguration trigger = projectB.getTrigger(DEPENDENCY_TRIGGER);
        trigger.setPropagateVersion(true);
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        xmlRpcHelper.waitForBuildToComplete(projectB.getConfig().getName(), 1);

        assertIvyInRepository(projectB, "FIXED");
        assertIvyRevision("FIXED", projectB, "FIXED");
    }

    public void testRepositoryFormat_OrgSpecified() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.getConfig().setOrganisation("org");
        project.addArtifacts("build/artifact.jar");
        project.addFilesToCreate("build/artifact.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        assertIvyInRepository(project, buildNumber);
    }

    public void testDirectoryArtifactRoundTrip() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addDirArtifact("dirArtifact", "build/blah");
        projectA.addFilesToCreate("build/blah/artifact-A.jar");
        projectA.addFilesToCreate("build/blah/artifact-B.jar");
        insertProject(projectA);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        assertIvyInRepository(projectA, buildNumber);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact-A.jar");
        projectB.addExpectedFiles("lib/artifact-B.jar");
        insertProject(projectB);

        buildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testArtifactWithNoExtension() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "Upstream");
        projectA.addArtifact("fileArtifact", "build/artifactWithNoExtension");
        projectA.addFilesToCreate("build/artifactWithNoExtension");
        insertProject(projectA);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        assertIvyInRepository(projectA, buildNumber);

        DepAntProject projectB = projects.createDepAntProject(randomName + "Downstream");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifactWithNoExtension");
        insertProject(projectB);

        buildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testDirectoryOfMixedExtensionArtifacts() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "Upstream");
        projectA.addDirArtifact("dirArtifact", "build/blah");
        projectA.addFilesToCreate("build/blah/artifact-A.jar");
        projectA.addFilesToCreate("build/blah/artifact-B");
        projectA.addFilesToCreate("build/blah/artifact-C.txt");
        insertProject(projectA);

        int buildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        assertIvyInRepository(projectA, buildNumber);

        DepAntProject projectB = projects.createDepAntProject(randomName + "Downstream");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/artifact-A.jar");
        projectB.addExpectedFiles("lib/artifact-B");
        projectB.addExpectedFiles("lib/artifact-C.txt");
        insertProject(projectB);

        buildNumber = buildRunner.triggerSuccessfulBuild(projectB.getConfig());

        assertIvyInRepository(projectB, buildNumber);
    }

    public void testArtifactPattern() throws Exception
    {
        DepAntProject project = projects.createDepAntProject(randomName);
        project.addArtifacts("build/artifact-12345.jar").get(0).setArtifactPattern("(.+)-[0-9]+\\.(.+)");
        project.addFilesToCreate("build/artifact-12345.jar");
        insertProject(project);

        int buildNumber = buildRunner.triggerSuccessfulBuild(project.getConfig());

        assertArtifactInRepository(project, "default", buildNumber, "artifact", "jar");
    }

    public void testUnusualCharactersInArtifactName() throws Exception
    {
        String supportedCharacters = "!()._-@#%^&";

        List<String> failedCharacters = new LinkedList<String>();
        String message = "";
        for (char c : supportedCharacters.toCharArray())
        {
            try
            {
                runTestForCharacterSupport(c);
            }
            catch (Exception e)
            {
                StringWriter sw = new StringWriter();
                sw.append('\n');
                e.printStackTrace(new PrintWriter(sw));
                message += sw.toString();
                failedCharacters.add(String.valueOf(c));
            }
        }
        assertEquals("Unexpected problems with characters: " + StringUtils.join("", failedCharacters.toArray(new String[failedCharacters.size()])) + message, 0, failedCharacters.size());
    }

    // CIB-2171
    public void testDependencyStatusUpdates() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/artifact.jar");
        projectA.addFilesToCreate("build/artifact.jar");
        projectA.getConfig().getDependencies().setStatus(STATUS_INTEGRATION);
        insertProject(projectA);

        buildRunner.triggerSuccessfulBuild(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        DependencyConfiguration dependency = projectB.addDependency(projectA.getConfig());
        dependency.setRevision(LATEST + STATUS_INTEGRATION);
        projectB.addExpectedFiles("lib/artifact-1.jar");
        projectB.getConfig().getDependencies().setRetrievalPattern("lib/[artifact]-[revision].[ext]");
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectB);

        dependency.setRevision(LATEST + STATUS_RELEASE);
        updateProject(projectB);

        buildRunner.triggerFailedBuild(projectB);

        // ensure that the 'retrieve' command is what failed.
        assertCommandFailed(projectB.getName(), 2, "retrieve");
    }

    // CIB-2194
    public void testDownstreamRetrieval() throws Exception
    {
        DepAntProject projectA = projects.createDepAntProject(randomName + "A");
        projectA.addArtifacts("build/projectA-artifact.jar");
        projectA.addFilesToCreate("build/projectA-artifact.jar");
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "B");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/projectA-artifact.jar");
        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectA);
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        buildRunner.triggerSuccessfulBuild(projectB);
    }

    public void testPropagateRevisionToDownstream() throws Exception
    {
        String revision = setupPropagateWorkspace();

        String projectAName = randomName + "-upstream";
        ProjectConfigurationHelper projectA = projects.createAntProject(projectAName, Constants.TRIVIAL_ANT_REPOSITORY + "/" + projectAName);
        insertProject(projectA);

        String projectBName = randomName + "-downstream";
        ProjectConfigurationHelper projectB = projects.createAntProject(projectBName, Constants.TRIVIAL_ANT_REPOSITORY + "/" + projectBName);
        projectB.addDependency(projectA);
        DependentBuildTriggerConfiguration trigger = projectB.getTrigger(DEPENDENCY_TRIGGER);
        trigger.setPropagateRevision(true);
        insertProject(projectB);

        // test triggering the upstream project.
        buildRunner.triggerSuccessfulBuild(projectA);
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1);

        // verify build revision.
        assertEquals(revision, xmlRpcHelper.getBuildRevision(projectAName, 1));
        assertEquals(revision, xmlRpcHelper.getBuildRevision(projectBName, 1));

        // test triggering the downstream project.
        buildRunner.triggerRebuild(projectB);
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 2);

        assertEquals(revision, xmlRpcHelper.getBuildRevision(projectAName, 2));
        assertEquals(revision, xmlRpcHelper.getBuildRevision(projectBName, 2));

        // test that triggering b directly picks up its own revision.
        buildRunner.triggerSuccessfulBuild(projectB);
        assertFalse(revision.equals(xmlRpcHelper.getBuildRevision(projectBName, 3)));
    }

    public void testPublishAndRetrievalOfLargeFiles() throws Exception
    {
        File largeArtifact = new File(tmp, "BIGFILE");
        FileOutputStream out = new FileOutputStream(largeArtifact, true);
        byte[] oneMegabyte = new byte[(int) MEGABYTE];
        for (int i = 0; i < 500; i++)
        {
            IOUtils.joinStreams(new ByteArrayInputStream(oneMegabyte), out);
        }

        DepAntProject projectA = projects.createDepAntProject(randomName + "-upstream");
        projectA.getRecipe(DEFAULT_RECIPE).addArtifact("BIG ARTIFACT", largeArtifact.getCanonicalPath().replace('\\', '/'));
        insertProject(projectA);

        DepAntProject projectB = projects.createDepAntProject(randomName + "-downstream");
        projectB.addDependency(projectA.getConfig());
        projectB.addExpectedFiles("lib/BIGFILE");
        insertProject(projectB);

        // double the timeout to give pulse enough time to capture and upload the large file.

        int buildNumber = buildRunner.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildToComplete(projectA.getName(), buildNumber, XmlRpcHelper.BUILD_TIMEOUT * 2);
        ResultState buildStatus = buildRunner.getBuildStatus(projectA, buildNumber);
        assertEquals(ResultState.SUCCESS, buildStatus);
        xmlRpcHelper.waitForBuildToComplete(projectB.getName(), 1, XmlRpcHelper.BUILD_TIMEOUT * 2);
    }

    // setup the repository for the propagate revision tests.
    public String setupPropagateWorkspace() throws IOException, SVNException
    {
        File wcDir = createTempDirectory();
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, "pulse", "pulse");
        try
        {
            workspace.doCheckout(TRIVIAL_ANT_REPOSITORY);

            File upstreamBuildFile = new File(wcDir, randomName + "-upstream/build.xml");
            assertTrue(upstreamBuildFile.getParentFile().mkdirs());
            FileSystemUtils.copy(upstreamBuildFile, new File(wcDir, "build.xml"));

            File downstreamBuildFile = new File(wcDir, randomName + "-downstream/build.xml");
            assertTrue(downstreamBuildFile.getParentFile().mkdirs());
            FileSystemUtils.copy(downstreamBuildFile, new File(wcDir, "build.xml"));

            workspace.doAdd(upstreamBuildFile.getParentFile(), upstreamBuildFile, downstreamBuildFile.getParentFile(), downstreamBuildFile);
            workspace.doCommit("initial checkin", upstreamBuildFile.getParentFile(), upstreamBuildFile, downstreamBuildFile.getParentFile(), downstreamBuildFile);

            File upstreamFile = new File(wcDir, randomName + "-upstream/file.txt");
            assertTrue(upstreamFile.createNewFile());
            workspace.doAdd(upstreamFile);
            String revision = workspace.doCommit("update upstream", upstreamFile);

            File downstreamFile = new File(wcDir, randomName + "-downstream/file.txt");
            assertTrue(downstreamFile.createNewFile());
            workspace.doAdd(downstreamFile);
            workspace.doCommit("update downstream", downstreamFile);

            return revision;
        }
        finally
        {
            IOUtils.close(workspace);
        }
    }

    private void assertCommandFailed(String projectName, int buildNumber, String commandName) throws Exception
    {
        Vector<Hashtable<String, String>> features = xmlRpcHelper.call("getErrorMessagesInBuild", projectName, buildNumber);
        for (Hashtable<String, String> feature : features)
        {
            if (feature.containsKey("command"))
            {
                assertEquals(commandName, feature.get("command"));
                assertEquals("error", feature.get("level"));
            }
        }
    }

    private void runTestForCharacterSupport(char c) throws Exception
    {
        String projectName = getName() + c + RandomUtils.randomString(5);
        DepAntProject projectA = projects.createDepAntProject(projectName + "A");
        projectA.setOrganisation("org" + c + "name");
        projectA.addArtifact("file", "build/artifactA-" + c + ".jar");
        projectA.addDirArtifact("dir", "build/dir");

        projectA.addStage("stage" + c + "name");

        // The ant script on unix evals its arguments, so we need to escape
        // these characters lest the shell choke on them.
        String resolvedChar = SystemUtils.IS_WINDOWS ? Character.toString(c) : "\\" + c;
        projectA.addFilesToCreate("build/artifactA-" + resolvedChar + ".jar", "build/dir/artifactB-" + resolvedChar + ".jar", "build/dir/artifactC-" + resolvedChar + ".jar");

        insertProject(projectA);

        long buildNumber = buildRunner.triggerSuccessfulBuild(projectA.getConfig());

        assertIvyInRepository(projectA, buildNumber);

        DepAntProject projectB = projects.createDepAntProject(projectName + "B");
        projectB.addDependency(projectA, "stage" + c + "name");
        projectB.addExpectedFiles("lib/artifactA-" + resolvedChar + ".jar", "lib/artifactB-" + resolvedChar + ".jar", "lib/artifactC-" + resolvedChar + ".jar");

        insertProject(projectB);

        buildRunner.triggerSuccessfulBuild(projectB.getConfig());
    }

    private void assertIvyStatus(String expectedStatus, ProjectConfigurationHelper project, int buildNumber) throws Exception
    {
        assertEquals(expectedStatus, repository.getIvyModuleDescriptor(project.getConfig().getOrganisation(), project.getConfig().getName(), buildNumber).getStatus());
    }

    private void assertIvyRevision(String expectedRevision, ProjectConfigurationHelper project, String version) throws Exception
    {
        assertEquals(expectedRevision, repository.getIvyModuleDescriptor(project.getConfig().getOrganisation(), project.getConfig().getName(), version).getRevision());
    }

    private void assertIvyInRepository(ProjectConfigurationHelper project, Object revision) throws Exception
    {
        assertInRepository(repository.getIvyModuleDescriptor(project.getConfig().getOrganisation(), project.getConfig().getName(), revision).getPath());
    }

    private void assertIvyNotInRepository(ProjectConfigurationHelper project, Object revision) throws Exception
    {
        assertNotInRepository(repository.getIvyModuleDescriptorPath(project.getConfig().getOrganisation(), project.getConfig().getName(), revision));
    }

    private void assertArtifactInRepository(ProjectConfigurationHelper project, String stageName, Object revision, String artifactName, String artifactExtension) throws IOException
    {
        assertInRepository(repository.getArtifactPath(project.getConfig().getOrganisation(), project.getConfig().getName(), stageName, revision, artifactName, artifactExtension));
    }

    private void assertArtifactNotInRepository(ProjectConfigurationHelper project, String stageName, Object revision, String artifactName, String artifactExtension) throws IOException
    {
        assertNotInRepository(repository.getArtifactPath(project.getConfig().getOrganisation(), project.getConfig().getName(), stageName, revision, artifactName, artifactExtension));
    }

    private void assertInRepository(String baseArtifactName) throws IOException
    {
        // all artifacts are being published with .md5 and .sha1 hashes.
        assertTrue(baseArtifactName + " not found in repository", repository.waitUntilInRepository(baseArtifactName));
        assertTrue(baseArtifactName + ".md5 not found in repository", repository.waitUntilInRepository(baseArtifactName + ".md5"));
        assertTrue(baseArtifactName + ".sha1 not found in repository", repository.waitUntilInRepository(baseArtifactName + ".sha1"));
    }

    public void assertNotInRepository(String baseArtifactName) throws IOException
    {
        assertFalse(repository.waitUntilInRepository(baseArtifactName));
    }
}
