package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import static com.zutubi.pulse.acceptance.Constants.Project.Dependencies.*;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.*;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String PROPERTY_CREATE_LIST = "create.list";
    private static final String PROPERTY_EXPECTED_LIST = "expected.list";
    private static final String PROPERTY_NOT_EXPECTED_LIST = "not.expected.list";

    private Repository repository;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        repository = new Repository();
        repository.clear();
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
        artifact.name = "artifact";
        artifact.extension = "jar";
        assertArtifactInRepository(project, project.getDefaultStage(), buildNumber, artifact);
    }

    private int triggerSuccessfulBuild(Project project, AntBuildConfiguration build) throws Exception
    {
        int buildNumber = triggerBuild(project, build);
        assertEquals(ResultState.SUCCESS, getBuildStatus(project.getName(), buildNumber));
        return buildNumber;
    }

    private int triggerSuccessfulBuild(Project project, AntBuildConfiguration build, String status) throws Exception
    {
        int buildNumber = triggerBuild(project, build, status);
        assertEquals(ResultState.SUCCESS, getBuildStatus(project.getName(), buildNumber));
        return buildNumber;
    }

    private int triggerBuild(Project project, AntBuildConfiguration build, String status) throws Exception
    {
        triggerBuildCommon(project, build);
        return xmlRpcHelper.runBuild(project.getName(), asPair("status", (Object)status));
    }

    private int triggerBuild(Project project, AntBuildConfiguration build) throws Exception
    {
        triggerBuildCommon(project, build);
        return xmlRpcHelper.runBuild(project.getName());
    }

    private void triggerBuildCommon(Project project, AntBuildConfiguration build) throws Exception
    {
        // for each stage, set the necessary build properties.
        for (Stage stage : project.stages)
        {
            stage.getRecipe();
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_CREATE_LIST, build.getCreateList());
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_EXPECTED_LIST, build.getExpectedList());
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_NOT_EXPECTED_LIST, build.getNotExpectedList());
        }
    }

    private void createProject(Project project) throws Exception
    {
        String target = "present not.present create";
        String args = "-Dcreate.list=\"${"+PROPERTY_CREATE_LIST+"}\" -Dpresent.list=\"${"+PROPERTY_EXPECTED_LIST+"}\" -Dnot.present.list=\"${"+PROPERTY_NOT_EXPECTED_LIST+"}\"";

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("name", "build");
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(project.getName(), ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);

        setProjectOrganisation(project);
        configureDependencies(project);

        for (Recipe recipe : project.getRecipes())
        {
            ensureRecipeExists(project, recipe, antConfig);
            for (Artifact artifact : recipe.getArtifacts())
            {
                String command = "build";
                addArtifact(project, recipe.getName(), command, artifact.getName(), artifact.getExtension(), artifact.getArtifactPattern());
            }
        }

        for (Stage stage : project.stages)
        {
            // create stage.
            ensureStageExists(project, stage);

            // set blank default properties.
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_CREATE_LIST, "");
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_EXPECTED_LIST, "");
            xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), PROPERTY_NOT_EXPECTED_LIST, "");

            // setup the rest of the properties configured for the stage.
            for (Map.Entry<String, String> entry : stage.properties.entrySet())
            {
                xmlRpcHelper.insertOrUpdateStageProperty(project.getName(), stage.getName(), entry.getKey(), entry.getValue());
            }
        }

        for (Dependency dependency : project.dependencies)
        {
            addDependency(project, dependency);
        }

        String triggersPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project.name, "triggers");
        Hashtable<String, Object> trigger = xmlRpcHelper.createEmptyConfig(DependentBuildTriggerConfiguration.class);
        trigger.put("name", "dependency trigger");
        trigger.put("propagateStatus", project.isPropagateStatus());
        trigger.put("propagateVersion", project.isPropagateVersion());
        xmlRpcHelper.insertConfig(triggersPath, trigger);
    }

    private void setProjectOrganisation(Project project) throws Exception
    {
        if (TextUtils.stringSet(project.getOrg()))
        {
            String path = "projects/" + project.getName();
            Hashtable<String, Object> projectConfig = xmlRpcHelper.getConfig(path);
            projectConfig.put(Constants.Project.ORGANISATION, project.getOrg());
            xmlRpcHelper.saveConfig(path, projectConfig, true);
        }
    }

    private void ensureRecipeExists(Project project, Recipe recipe, Hashtable<String, Object> commandConfig) throws Exception
    {
        String recipePath = "projects/" + project.getName() + "/type/recipes/" + recipe.getName();
        if (!xmlRpcHelper.configPathExists(recipePath))
        {
            Hashtable<String, Object> recipeConfig = xmlRpcHelper.createDefaultConfig(RecipeConfiguration.class);
            recipeConfig.put(Constants.Project.MultiRecipeType.NAME, recipe.getName());

            Hashtable<String, Object> commands = new Hashtable<String, Object>();
            commands.put((String)commandConfig.get("name"), commandConfig);
            recipeConfig.put("commands", commands);

            xmlRpcHelper.insertConfig("projects/" + project.getName() + "/type/recipes", recipeConfig);
        }
    }

    public void ensureStageExists(Project project, Stage stage) throws Exception
    {
        // configure the default stage.
        String stagePath = "projects/" + project.getName() + "/stages/" + stage.getName();
        if (!xmlRpcHelper.configPathExists(stagePath))
        {
            Hashtable<String, Object> stageConfig = xmlRpcHelper.createDefaultConfig(BuildStageConfiguration.class);
            stageConfig.put(Constants.Project.Stage.NAME, stage.getName());
            stageConfig.put(Constants.Project.Stage.RECIPE, stage.getRecipe().getName());
            xmlRpcHelper.insertConfig("projects/" + project.getName() + "/stages", stageConfig);
        }
    }

    public void configureDependencies(Project project) throws Exception
    {
        // configure the default stage.
        String dependenciesPath = "projects/" + project.getName() + "/dependencies";
        Hashtable<String, Object> dependencies = xmlRpcHelper.getConfig(dependenciesPath);
        dependencies.put(RETRIEVAL_PATTERN, project.retrievalPattern);
        if (TextUtils.stringSet(project.status))
        {
            dependencies.put(STATUS, project.status);
        }
        if (TextUtils.stringSet(project.version))
        {
            dependencies.put(VERSION, project.version);
        }
        xmlRpcHelper.saveConfig(dependenciesPath, dependencies, false);
    }

    private void addArtifact(Project project, String recipe, String command, String artifactName, String artifactExtension, String pattern) throws Exception
    {
        String artifactsPath = "projects/" + project.getName() + "/type/recipes/" + recipe + "/commands/" + command + "/outputs";

        Hashtable<String, Object> artifactData = xmlRpcHelper.createDefaultConfig(FileOutputConfiguration.class);
        artifactData.put("name", artifactName);
        artifactData.put("file", "build/" + artifactName + "." + artifactExtension);
        artifactData.put("publish", true);
        if (pattern != null)
        {
            artifactData.put("artifactPattern", pattern);
        }

        xmlRpcHelper.insertConfig(artifactsPath, artifactData);
    }

    private void addDependency(Project project, Dependency projectDependency) throws Exception
    {
        // configure the default stage.
        String projectDependenciesPath = "projects/" + project.getName() + "/dependencies";

        Hashtable<String, Object> projectDependencies = xmlRpcHelper.getConfig(projectDependenciesPath);
        if (!projectDependencies.containsKey("dependencies"))
        {
            projectDependencies.put("dependencies", new Vector<Hashtable<String, Object>>());
        }

        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> dependencies = (Vector<Hashtable<String, Object>>) projectDependencies.get("dependencies");

        Hashtable<String, Object> dependency = xmlRpcHelper.createEmptyConfig(DependencyConfiguration.class);
        dependency.put("project", "projects/" + projectDependency.project.getName());
        dependency.put("revision", projectDependency.revision);
        dependency.put("allStages", (projectDependency.stage == null));
        dependency.put("stages", asStagePaths(projectDependency));
        dependency.put("transitive", projectDependency.transitive);
        dependencies.add(dependency);

        xmlRpcHelper.saveConfig(projectDependenciesPath, projectDependencies, true);
    }

    private Vector<String> asStagePaths(Dependency dependency)
    {
        Vector<String> v = new Vector<String>();
        if (dependency.stage != null)
        {
            v.add("projects/" + dependency.project.name + "/stages/" + dependency.stage);
        }
        return v;
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

    /**
     * The project model used by these tests to simplify management of the test configuration.
     * This model differs from the ProjectConfiguration in that only properties used by this
     * test suite are available.
     */
    private class Project
    {
        private String name;
        private String org;
        private String status;
        private String version;
        private List<Dependency> dependencies = new LinkedList<Dependency>();

        private List<Stage> stages = new LinkedList<Stage>();
        private List<Recipe> recipes = new LinkedList<Recipe>();

        private boolean propagateStatus = false;
        private boolean propagateVersion = false;

        private String retrievalPattern = "lib/[artifact].[ext]";

        private Project(String name)
        {
            this.setName(name);
            addStage("default");
            addRecipe("default");
        }

        private Project(String name, String org)
        {
            this(name);
            this.setOrg(org);
        }

        public Recipe addRecipe(String recipeName)
        {
            Recipe recipe = new Recipe(this, recipeName);
            this.recipes.add(recipe);
            return recipe;
        }

        public Stage addStage(String stageName)
        {
            Stage stage = new Stage(this, stageName);
            this.stages.add(stage);
            return stage;
        }

        private void addDependency(Project dependency)
        {
            dependencies.add(new Dependency(dependency));
        }

        private void addDependency(Dependency dependnecy)
        {
            dependencies.add(dependnecy);
        }

        private Stage getDefaultStage()
        {
            return getStage("default");
        }

        private Artifact addArtifact(String artifact)
        {
            return getRecipe("default").addArtifact(artifact);
        }

        private List<Artifact> addArtifacts(String... artifacts)
        {
            return getRecipe("default").addArtifacts(artifacts);
        }

        private void setRetrievalPattern(String retrievalPattern)
        {
            this.retrievalPattern = retrievalPattern;
        }

        private String getName()
        {
            return name;
        }

        private void setName(String name)
        {
            this.name = name;
        }

        private String getOrg()
        {
            return org;
        }

        private void setOrg(String org)
        {
            this.org = org;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

        public void setVersion(String version)
        {
            this.version = version;
        }

        public boolean isPropagateStatus()
        {
            return propagateStatus;
        }

        public void setPropagateStatus(boolean propagateStatus)
        {
            this.propagateStatus = propagateStatus;
        }

        public boolean isPropagateVersion()
        {
            return propagateVersion;
        }

        public void setPropagateVersion(boolean b)
        {
            this.propagateVersion = b;
        }

        public Stage getStage(final String stageName)
        {
            return CollectionUtils.find(stages, new Predicate<Stage>()
            {
                public boolean satisfied(Stage stage)
                {
                    return stage.getName().equals(stageName);
                }
            });
        }
        public Recipe getRecipe(final String recipeName)
        {
            return CollectionUtils.find(recipes, new Predicate<Recipe>()
            {
                public boolean satisfied(Recipe recipe)
                {
                    return recipe.getName().equals(recipeName);
                }
            });
        }

        public List<Recipe> getRecipes()
        {
            return recipes;
        }

        public Recipe getDefaultRecipe()
        {
            return getRecipe("default");
        }
    }

    private class Dependency
    {
        private Project project;

        private boolean transitive = true;

        private String stage = null;

        private String revision = DependencyConfiguration.LATEST_INTEGRATION;

        private Dependency(Project project, boolean transitive, String stage, String revision)
        {
            this(project, transitive, stage);
            this.revision = revision;
        }

        private Dependency(Project project, boolean transitive, String stage)
        {
            this(project, transitive);
            this.stage = stage;
        }

        private Dependency(Project project, boolean transitive)
        {
            this(project);
            this.transitive = transitive;
        }

        private Dependency(Project project)
        {
            this.project = project;
        }
    }

    private class Stage
    {
        private Project project;
        private String name;
        private Recipe recipe;

        private Map<String, String> properties = new HashMap<String, String>();

        private Stage(Project project, String name)
        {
            this.setName(name);
            this.setProject(project);
        }

        private void addProperty(String name, String value)
        {
            properties.put(name, value);
        }

        private String getName()
        {
            return name;
        }

        private void setName(String name)
        {
            this.name = name;
        }

        public Project getProject()
        {
            return project;
        }

        public void setProject(Project project)
        {
            this.project = project;
        }

        public Recipe getRecipe()
        {
            if (recipe != null)
            {
                return recipe;
            }
            return project.getDefaultRecipe();
        }

        public void setRecipe(Recipe recipe)
        {
            this.recipe = recipe;
        }
    }

    private class Recipe
    {
        private Project project;
        private String name;
        private List<Artifact> artifacts = new LinkedList<Artifact>();

        private Recipe(Project project, String name)
        {
            this.project = project;
            this.name = name;
        }

        public Artifact addArtifact(String artifactName)
        {
            Artifact artifact = new Artifact(artifactName, this);
            this.artifacts.add(artifact);
            return artifact;
        }

        public List<Artifact> addArtifacts(String... artifactNames)
        {
            List<Artifact> artifacts = new LinkedList<Artifact>();
            for (String artifactName : artifactNames)
            {
                artifacts.add(addArtifact(artifactName));
            }
            return artifacts;
        }

        public List<Artifact> getArtifacts()
        {
            return artifacts;
        }

        public String getName()
        {
            return name;
        }
    }

    private class Artifact
    {
        private final Pattern pattern = Pattern.compile("(.+)\\.(.+)");

        private String name;
        private String extension;
        private Recipe recipe;
        private String artifactPattern;

        private Artifact(String filename, Recipe recipe)
        {
            this.recipe = recipe;
            Matcher m = pattern.matcher(filename);
            if (m.matches())
            {
                name = m.group(1);
                extension = m.group(2);
            }
        }

        public String getName()
        {
            return name;
        }

        public String getExtension()
        {
            return extension;
        }

        public Recipe getRecipe()
        {
            return recipe;
        }

        public String getArtifactPattern()
        {
            return artifactPattern;
        }

        public void setArtifactPattern(String artifactPattern)
        {
            this.artifactPattern = artifactPattern;
        }
    }

    /**
     * Contains the configuration details to be passed through to the ant build
     * to a) produce the specified artifacts for each stage and b) to assert the
     * existance of the specified artifacts
     */
    private class AntBuildConfiguration
    {
        /**
         * The list of files to be created by the ant build.
         */
        private List<String> filesToCreate = new LinkedList<String>();
        /**
         * The list of files whose presence is to be asserted by the build.  If any of these
         * files are missing, the build fails.
         */
        private List<String> expectedFiles = new LinkedList<String>();
        /**
         * The list of files whose absence is asserted by the build.  If any of these
         * files are present, the build fails.
         */
        private List<String> notExpected = new LinkedList<String>();

        private void addFileToCreate(String artifact)
        {
            filesToCreate.add(artifact);
        }

        private void addFilesToCreate(String... artifacts)
        {
            this.filesToCreate.addAll(Arrays.asList(artifacts));
        }

        private void addExpectedFile(String dependency)
        {
            expectedFiles.add(dependency);
        }

        private void addExpectedFiles(String... dependencies)
        {
            this.expectedFiles.addAll(Arrays.asList(dependencies));
        }

        private void addNotExpectedFile(String file)
        {
            this.notExpected.add(file);
        }

        private String getCreateList()
        {
            return StringUtils.join(",", filesToCreate);
        }

        private String getExpectedList()
        {
            return StringUtils.join(",", expectedFiles);
        }

        private String getNotExpectedList()
        {
            return StringUtils.join(",", notExpected);
        }
    }
}
