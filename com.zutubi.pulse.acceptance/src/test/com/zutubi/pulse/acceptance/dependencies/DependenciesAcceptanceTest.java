package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import static com.zutubi.pulse.acceptance.Constants.Project.Dependencies.*;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.*;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.ivyPath;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.*;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.PublicationConfiguration;
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

public class DependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String PROPERTY_CREATE_LIST = "create.list";
    private static final String PROPERTY_EXPECTED_LIST = "expected.list";
    private static final String PROPERTY_NOT_EXPECTED_LIST = "not.expected.list";

    /**
     * Timeout waiting for an artifact to appear in the repository.
     */
    private static final int AVAILABILITY_TIMEOUT = 5000;

    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();

        clearArtifactRepository();
    }

    public void testPublish_NoArtifacts() throws Exception
    {
        // configure project.
        Project project = new Project(randomName());
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();

        int buildNumber = triggerSuccessfulBuild(project, build);

        // verify existance of expected artifacts.
        assertIvyInRepository(project.getName(), buildNumber);
    }

    public void testPublish_SingleArtifact() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
    }

    public void testPublish_MultipleArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifacts("artifact.jar", "another-artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");
        build.addFileToCreate("build/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "another-artifact", buildNumber);
    }

    public void testPublish_MultipleStages() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifact("default", "artifact.jar");
        project.addArtifact("stage", "artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), "default", "artifact", buildNumber);
        assertJarInRepository(project.getName(), "stage", "artifact", buildNumber);
    }

    public void testPublish_CustomPublicationPattern() throws Exception
    {
        Project project = new Project(randomName());
        project.setPublicationPattern("my/build/[artifact].[ext]");
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("my/build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
    }

    public void testPublish_CustomPublicationPatternViaProperty() throws Exception
    {
        Project project = new Project(randomName());
        project.setPublicationPattern("${publicationPattern}");
        project.getDefaultStage().addArtifacts("artifact.jar");
        project.getDefaultStage().addProperty("publicationPattern", "custom/build/[artifact].[ext]");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("custom/build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
    }

    public void testPublishFails_MissingArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("incorrect/path/artifact.jar");

        int buildNumber = triggerBuild(project, build);
        assertTrue(isBuildErrored(project.getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(project.getName(), buildNumber);
        assertJarNotInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
    }

    public void testPublish_ConfigurationAtProjectLevel() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
    }

    public void testPublish_ConfigurationAtProjectLevelAdditive() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts("artifact.jar");
        project.getDefaultStage().addArtifacts("another-artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");
        build.addFileToCreate("build/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "artifact", buildNumber);
        assertJarInRepository(project.getName(), project.getDefaultStage().getName(), "another-artifact", buildNumber);
    }

    public void testPublish_StatusConfiguration() throws Exception
    {
        Project project = new Project(randomName());
        project.setStatus(STATUS_RELEASE);
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertIvyStatus(STATUS_RELEASE, project.getName(), buildNumber);
    }

    public void testPublish_DefaultStatus() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyStatus(STATUS_INTEGRATION, project.getName(), buildNumber);
    }

    public void testStatusValidation() throws Exception
    {
        try
        {
            Project project = new Project(randomName());
            project.setStatus("invalid");
            project.getDefaultStage().addArtifacts("artifact.jar");
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
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build, STATUS_MILESTONE);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.getName(), buildNumber);
        assertIvyStatus(STATUS_MILESTONE, project.getName(), buildNumber);
    }

    public void testRetrieve_SingleArtifact() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("artifact.jar");
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

        assertIvyInRepository(projectB.getName(), buildNumber);
    }

    public void testRetrieve_MultipleArtifacts() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("artifact.jar", "another-artifact.jar");
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

        assertIvyInRepository(projectB.getName(), buildNumber);
    }

    public void testRetrieve_SpecificStage() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifact("default", "default-artifact.jar");
        projectA.addArtifact("stage", "stage-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/default-artifact.jar");
        buildA.addFileToCreate("build/stage-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addDependency(new Dependency(projectA, true, "stage"));
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addNotExpectedFile("lib/default-artifact.jar");
        buildB.addExpectedFiles("lib/stage-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);
    }

    public void testRetrieve_SpeicificRevision() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifact("default", "default-artifact.jar");
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
        projectA.getDefaultStage().addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.getDefaultStage().addArtifacts("projectB-artifact.jar");
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
        assertIvyInRepository(projectC.getName(), buildNumber);
    }

    public void testRetrieve_TransitiveDependencies() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.getDefaultStage().addArtifacts("projectB-artifact.jar");
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
        assertIvyInRepository(projectC.getName(), buildNumber);
    }

    public void testRetrieve_TransitiveDependenciesDisabled() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        AntBuildConfiguration buildA = new AntBuildConfiguration();
        buildA.addFileToCreate("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.getDefaultStage().addArtifacts("projectB-artifact.jar");
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
        assertIvyInRepository(projectC.getName(), buildNumber);
    }

    public void testRetrieveFails_MissingDependencies() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("artifact.jar");
        createProject(projectA);

        // do not build project a simulating dependency not available.

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        AntBuildConfiguration buildB = new AntBuildConfiguration();
        buildB.addExpectedFile("lib/artifact.jar");

        int buildNumber = triggerBuild(projectB, buildB);
        assertTrue(isBuildFailure(projectB.getName(), buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(projectB.getName(), buildNumber);
    }

    public void testDependentBuild_TriggeredOnSuccess() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("artifact.jar");
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
        projectA.getDefaultStage().addArtifacts("artifact.jar");
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

        assertIvyStatus(STATUS_RELEASE, projectB.getName(), 1);
    }

    public void testDependentBuild_PropagateVersion() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("artifact.jar");
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

        assertInRepository(ivyPath(projectB.getName(), "FIXED"));
    }

    public void testRepositoryFormat_OrgSpecified() throws Exception
    {
        Project project = new Project(randomName(), "org");
        project.getDefaultStage().addArtifacts("artifact.jar");
        createProject(project);

        AntBuildConfiguration build = new AntBuildConfiguration();
        build.addFileToCreate("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertInRepository(project.getOrg() + "/" + project.getName() + "/ivy-" + buildNumber + ".xml");
    }

    private int triggerSuccessfulBuild(Project project, AntBuildConfiguration build) throws Exception
    {
        int buildNumber = triggerBuild(project, build);
        assertTrue(isBuildSuccessful(project.getName(), buildNumber));
        return buildNumber;
    }

    private int triggerSuccessfulBuild(Project project, AntBuildConfiguration build, String status) throws Exception
    {
        int buildNumber = triggerBuild(project, build, status);
        assertTrue(isBuildSuccessful(project.getName(), buildNumber));
        return buildNumber;
    }

    private int triggerBuild(Project project, AntBuildConfiguration build, String status) throws Exception
    {
        triggerBuildCommon(project, build);

        return xmlRpcHelper.runBuild(project.getName(), asPair("status", (Object)status));
    }

    private int triggerBuild(Project project, AntBuildConfiguration build) throws Exception
    {
        // for each stage, set the necessary build properties.
        triggerBuildCommon(project, build);

        return xmlRpcHelper.runBuild(project.getName());
    }

    private void triggerBuildCommon(Project project, AntBuildConfiguration build) throws Exception
    {
        // for each stage, set the necessary build properties.
        for (Stage stage : project.stages)
        {
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
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(project.getName(), ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);

        if (TextUtils.stringSet(project.getOrg()))
        {
            setProjectOrganisation(project);
        }

        configureDependencies(project);

        for (String artifact : project.artifacts)
        {
            addPublication(project.getName(), artifact);
        }

        for (Stage stage : project.stages)
        {
            // create stage.
            ensureStageExists(project.getName(), stage.getName());

            for (String artifact : stage.artifacts)
            {
                addPublication(project.getName(), stage.getName(), artifact);
            }

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
            addDependency(project.getName(), dependency);
        }

        String triggersPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project.name, "triggers");
        Hashtable<String, Object> trigger = xmlRpcHelper.createEmptyConfig(DependentBuildTriggerConfiguration.class);
        trigger.put("name", "dependency trigger");
        trigger.put("propagateStatus", project.isPropagateStatus());
        trigger.put("propagateVersion", project.isPropagateVersion());
        xmlRpcHelper.insertConfig(triggersPath, trigger);
    }

    private void setProjectOrganisation(Project p) throws Exception
    {
        String path = "projects/" + p.getName();
        Hashtable<String, Object> projectConfig = xmlRpcHelper.getConfig(path);
        projectConfig.put("organisation", p.getOrg());

        xmlRpcHelper.saveConfig(path, projectConfig, true);
    }

    public void ensureStageExists(String projectName, String stageName) throws Exception
    {
        // configure the default stage.
        String stagePath = "projects/" + projectName + "/stages/" + stageName;
        if (!xmlRpcHelper.configPathExists(stagePath))
        {
            Hashtable<String, Object> stage = xmlRpcHelper.createDefaultConfig(BuildStageConfiguration.class);
            stage.put(Constants.Project.Stage.NAME, stageName);

            xmlRpcHelper.insertConfig("projects/" + projectName + "/stages", stage);
        }
    }

    public void configureDependencies(Project project) throws Exception
    {
        // configure the default stage.
        String dependenciesPath = "projects/" + project.getName() + "/dependencies";
        Hashtable<String, Object> dependencies = xmlRpcHelper.getConfig(dependenciesPath);
        dependencies.put(PUBLICATION_PATTERN, project.publicationPattern);
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

    private void addPublication(String projectName, String stageName, String artifact) throws Exception
    {
        String stagePath = "projects/" + projectName + "/stages/" + stageName;
        addPublicationToPath(stagePath, artifact);
    }

    private void addPublication(String projectName, String artifact) throws Exception
    {
        String dependenciesPath = "projects/" + projectName + "/dependencies";
        addPublicationToPath(dependenciesPath, artifact);
    }

    private void addPublicationToPath(String path, String artifact) throws Exception
    {
        Hashtable<String, Object> publicationContainer = xmlRpcHelper.getConfig(path);
        if (!publicationContainer.containsKey("publications"))
        {
            publicationContainer.put("publications", new Vector<Hashtable<String, Object>>());
        }
        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> publications = (Vector<Hashtable<String, Object>>) publicationContainer.get("publications");
        publications.add(createPublication(artifact));

        xmlRpcHelper.saveConfig(path, publicationContainer, true);
    }

    private Hashtable<String, Object> createPublication(String artifact)
    {
        Hashtable<String, Object> jar = xmlRpcHelper.createEmptyConfig(PublicationConfiguration.class);
        jar.put("name", artifact.substring(0, artifact.lastIndexOf(".")));
        jar.put("ext", artifact.substring(artifact.lastIndexOf(".") + 1));
        return jar;
    }

    private void addDependency(String projectName, Dependency projectDependency) throws Exception
    {
        // configure the default stage.
        String projectDependenciesPath = "projects/" + projectName + "/dependencies";

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

    private void assertIvyStatus(String status, String projectName, int buildNumber) throws IOException
    {
        assertThat(getIvyFile(projectName, buildNumber), containsString("status=\""+status+"\""));
    }

    private void assertIvyInRepository(String projectName, int buildNumber) throws IOException
    {
        assertInRepository(ivyPath(projectName, buildNumber));
    }

    private void assertIvyNotInRepository(String projectName, int buildNumber) throws IOException
    {
        assertNotInRepository(ivyPath(projectName, buildNumber));
    }

    private void assertJarInRepository(String projectName, String stageName, String artifactName, int buildNumber) throws IOException
    {
        assertInRepository(projectName + "/" + IvyUtils.ivyEncodeStageName(stageName) + "/jars/" + artifactName + "-" + buildNumber + ".jar");
    }

    private void assertJarNotInRepository(String projectName, String stageName, String artifactName, int buildNumber) throws IOException
    {
        assertNotInRepository(projectName + "/" + IvyUtils.ivyEncodeStageName(stageName) + "/jars/" + artifactName + "-" + buildNumber + ".jar");
    }

    private void assertInRepository(String baseArtifactName) throws IOException
    {
        // all artifacts are being published with .md5 and .sha1 hashes.
        assertTrue(waitUntilInRepository(baseArtifactName, AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(baseArtifactName + ".md5", AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(baseArtifactName + ".sha1", AVAILABILITY_TIMEOUT));
    }

    public void assertNotInRepository(String baseArtifactName) throws IOException
    {
        assertFalse(waitUntilInRepository(baseArtifactName, AVAILABILITY_TIMEOUT));
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
        private List<String> artifacts = new LinkedList<String>();
        private List<Stage> stages = new LinkedList<Stage>();
        private Stage defaultStage = new Stage("default");

        private boolean propagateStatus = false;
        private boolean propagateVersion = false;

        private String publicationPattern = "build/[artifact].[ext]";

        private String retrievalPattern = "lib/[artifact].[ext]";

        private Project(String name)
        {
            this.setName(name);
            this.stages.add(getDefaultStage());
        }

        private Project(String name, String org)
        {
            this(name);
            this.setOrg(org);
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
            return defaultStage;
        }

        private void addArtifacts(String... artifacts)
        {
            this.artifacts.addAll(Arrays.asList(artifacts));
        }

        private void addArtifact(String stageName, String artifactName)
        {
            getStage(stageName).addArtifacts(artifactName);
        }

        private Stage getStage(final String name)
        {
            Stage stage = CollectionUtils.find(stages, new Predicate<Stage>()
            {
                public boolean satisfied(Stage stage)
                {
                    return stage.getName().equals(name);
                }
            });

            if (stage == null)
            {
                stage = new Stage(name);
                stages.add(stage);
            }
            return stage;
        }

        private void setRetrievalPattern(String retrievalPattern)
        {
            this.retrievalPattern = retrievalPattern;
        }

        private void setPublicationPattern(String publicationPattern)
        {
            this.publicationPattern = publicationPattern;
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
        private String name;

        private List<String> artifacts = new LinkedList<String>();

        private Map<String, String> properties = new HashMap<String, String>();

        private Stage(String name)
        {
            this.setName(name);
        }

        private void addProperty(String name, String value)
        {
            properties.put(name, value);
        }

        private void addArtifacts(String... names)
        {
            artifacts.addAll(Arrays.asList(names));
        }

        private String getName()
        {
            return name;
        }

        private void setName(String name)
        {
            this.name = name;
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
