package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.clearArtifactRepository;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.waitUntilInRepository;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.util.*;

import java.io.IOException;
import java.util.*;

public class DependenciesAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    /**
     * Timeout waiting for a build to reach the expected state.
     */
    private static final int BUILD_TIMEOUT = 90000;
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

        Build build = new Build();

        int buildNumber = triggerSuccessfulBuild(project, build);

        // verify existance of expected artifacts.
        assertIvyInRepository(project.name, buildNumber);
    }

    public void testPublish_SingleArtifact() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifact("artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublish_MultipleArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifacts("artifact.jar", "another-artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("build/artifact.jar");
        build.addArtifact("build/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "another-artifact", buildNumber);
    }

    public void testPublish_MultipleStages() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifact("default", "artifact.jar");
        project.addArtifact("stage", "artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, "default", "artifact", buildNumber);
        assertJarInRepository(project.name, "stage", "artifact", buildNumber);
    }

    public void testPublish_CustomPublicationPattern() throws Exception
    {
        Project project = new Project(randomName());
        project.setPublicationPattern("my/build/[artifact].[ext]");
        project.getDefaultStage().addArtifact("artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("my/build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublish_CustomPublicationPatternViaProperty() throws Exception
    {
        Project project = new Project(randomName());
        project.setPublicationPattern("${publicationPattern}");
        project.getDefaultStage().addArtifact("artifact.jar");
        project.defaultStage.addProperty("publicationPattern", "custom/build/[artifact].[ext]");
        createProject(project);

        Build build = new Build();
        build.addArtifact("custom/build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublishFails_MissingArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        project.getDefaultStage().addArtifact("artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("incorrect/path/artifact.jar");

        int buildNumber = triggerBuild(project, build);
        assertTrue(isBuildErrored(project.name, buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(project.name, buildNumber);
        assertJarNotInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublish_ConfigurationAtProjectLevel() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts("artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublish_ConfigurationAtProjectLevelAdditive() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts("artifact.jar");
        project.getDefaultStage().addArtifacts("another-artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("build/artifact.jar");
        build.addArtifact("build/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "another-artifact", buildNumber);

    }

    public void testRetrieve_SingleArtifact() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifact("artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifact("build/artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        Build build = new Build();
        build.addDependency("lib/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectB, build);

        assertIvyInRepository(projectB.name, buildNumber);
    }

    public void testRetrieve_MultipleArtifacts() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("artifact.jar", "another-artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifacts("build/artifact.jar", "build/another-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        Build buildB = new Build();
        buildB.addDependencies("lib/artifact.jar", "lib/another-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectB, buildB);

        assertIvyInRepository(projectB.name, buildNumber);
    }

    public void testRetrieve_SpecificStage() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifact("default", "default-artifact.jar");
        projectA.addArtifact("stage", "stage-artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifact("build/default-artifact.jar");
        buildA.addArtifact("build/stage-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA, true, "stage");
        createProject(projectB);

        Build buildB = new Build();
        buildB.addNotExpected("lib/default-artifact.jar");
        buildB.addDependencies("lib/stage-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);
    }

    public void testRetrieve_SpeicificRevision() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifact("default", "default-artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifact("build/default-artifact.jar");
        
        // build twice and then depend on the first.
        int buildNumber = triggerSuccessfulBuild(projectA, buildA);
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.setRetrievalPattern("lib/[artifact]-[revision].[ext]");
        projectB.addDependency(new Dependency(projectA, true, "default", "" + buildNumber));
        createProject(projectB);

        Build buildB = new Build();
        buildB.addDependencies("lib/default-artifact-" + buildNumber + ".jar");
        triggerSuccessfulBuild(projectB, buildB);
    }

    public void testRetrieve_MultipleProjects() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifact("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.getDefaultStage().addArtifacts("projectB-artifact.jar");
        createProject(projectB);

        Build buildB = new Build();
        buildB.addArtifact("build/projectB-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);

        Project projectC = new Project(randomName());
        projectC.addDependency(projectA);
        projectC.addDependency(projectB);
        createProject(projectC);

        Build buildC = new Build();
        buildC.addDependencies("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectC, buildC);
        assertIvyInRepository(projectC.name, buildNumber);
    }

    public void testRetrieve_TransitiveDependencies() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifact("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.getDefaultStage().addArtifact("projectB-artifact.jar");
        projectB.addDependency(projectA, true);
        createProject(projectB);

        Build buildB = new Build();
        buildB.addArtifact("build/projectB-artifact.jar");
        buildB.addDependency("lib/projectA-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);

        Project projectC = new Project(randomName());
        projectC.addDependency(projectB);
        createProject(projectC);

        Build buildC = new Build();
        buildC.addDependencies("lib/projectA-artifact.jar", "lib/projectB-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectC, buildC);
        assertIvyInRepository(projectC.name, buildNumber);
    }

    public void testRetrieve_TransitiveDependenciesDisabled() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifacts("projectA-artifact.jar");
        createProject(projectA);

        Build buildA = new Build();
        buildA.addArtifact("build/projectA-artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        Project projectB = new Project(randomName());
        projectB.getDefaultStage().addArtifact("projectB-artifact.jar");
        projectB.addDependency(projectA);
        createProject(projectB);

        Build buildB = new Build();
        buildB.addArtifact("build/projectB-artifact.jar");
        buildB.addDependency("lib/projectA-artifact.jar");
        triggerSuccessfulBuild(projectB, buildB);

        Project projectC = new Project(randomName());
        projectC.addDependency(projectB, false);
        createProject(projectC);

        Build buildC = new Build();
        buildC.addDependencies("lib/projectB-artifact.jar");
        buildC.addNotExpected("lib/projectA-artifact.jar");

        int buildNumber = triggerSuccessfulBuild(projectC, buildC);
        assertIvyInRepository(projectC.name, buildNumber);
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

        Build buildB = new Build();
        buildB.addDependency("lib/artifact.jar");

        int buildNumber = triggerBuild(projectB, buildB);
        assertTrue(isBuildFailure(projectB.name, buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(projectB.name, buildNumber);
    }

    public void testDependentBuild_TriggeredOnSuccess() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.getDefaultStage().addArtifact("artifact.jar");
        createProject(projectA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        Build buildA = new Build();
        buildA.addArtifact("build/artifact.jar");
        triggerSuccessfulBuild(projectA, buildA);

        xmlRpcHelper.waitForBuildToComplete(projectB.name, 1, BUILD_TIMEOUT);
    }

    public void testRepositoryFormat_OrgSpecified() throws Exception
    {
        Project project = new Project(randomName(), "org");
        project.getDefaultStage().addArtifact("artifact.jar");
        createProject(project);

        Build build = new Build();
        build.addArtifact("build/artifact.jar");

        int buildNumber = triggerSuccessfulBuild(project, build);

        assertInRepository(project.org + "/" + project.name + "/ivy-" + buildNumber + ".xml");
    }

    private int triggerSuccessfulBuild(Project project, Build build) throws Exception
    {
        int buildNumber = triggerBuild(project, build);
        assertTrue(isBuildSuccessful(project.name, buildNumber));
        build.setBuildNumber(buildNumber);
        return buildNumber;
    }

    private int triggerBuild(Project project, Build build) throws Exception
    {
        // for each stage, set the necessary build properties.
        for (Stage stage : project.stages)
        {
            xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, "artifact.list", build.getArtifactList());
            xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, "dependency.list", build.getDependencyList());
            xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, "not.present.list", build.getNotExpectedList());
        }

        return xmlRpcHelper.runBuild(project.name, BUILD_TIMEOUT);
    }

    private void createProject(Project project) throws Exception
    {
        String target = "present not.present create";
        String args = "-Dcreate.list=\"${artifact.list}\" -Dpresent.list=\"${dependency.list}\" -Dnot.present.list=\"${not.present.list}\"";

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(project.name, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);

        if (TextUtils.stringSet(project.org))
        {
            setProjectOrganisation(project);
        }

        configureDependencies(project);

        for (String artifact : project.artifacts)
        {
            addPublication(project.name, artifact);
        }

        for (Stage stage : project.stages)
        {
            // create stage.
            ensureStageExists(project.name, stage.name);

            for (String artifact : stage.artifacts)
            {
                addPublication(project.name, stage.name, artifact);
            }

            // set blank default properties.
            xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, "artifact.list", "");
            xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, "dependency.list", "");
            xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, "not.present.list", "");

            // setup the rest of the properties configured for the stage.
            for (Map.Entry<String, String> entry : stage.properties.entrySet())
            {
                xmlRpcHelper.insertOrUpdateStageProperty(project.name, stage.name, entry.getKey(), entry.getValue());
            }
        }

        for (Dependency dependency : project.dependencies)
        {
            addDependency(project.name, dependency);
        }
    }

    private void setProjectOrganisation(Project p) throws Exception
    {
        String path = "projects/" + p.name;
        Hashtable<String, Object> projectConfig = xmlRpcHelper.getConfig(path);
        projectConfig.put("organisation", p.org);

        xmlRpcHelper.saveConfig(path, projectConfig, true);
    }

    public void ensureStageExists(String projectName, String stageName) throws Exception
    {
        // configure the default stage.
        String stagePath = "projects/" + projectName + "/stages/" + stageName;
        if (!xmlRpcHelper.configPathExists(stagePath))
        {
            Hashtable<String, Object> stage = xmlRpcHelper.createDefaultConfig(BuildStageConfiguration.class);
            stage.put("name", stageName);

            xmlRpcHelper.insertConfig("projects/" + projectName + "/stages", stage);
        }
    }

    public void configureDependencies(Project project) throws Exception
    {
        // configure the default stage.
        String dependenciesPath = "projects/" + project.name + "/dependencies";
        Hashtable<String, Object> dependencies = xmlRpcHelper.getConfig(dependenciesPath);
        dependencies.put("publicationPattern", project.publicationPattern);
        dependencies.put("retrievalPattern", project.retrievalPattern);
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
        Hashtable<String, Object> jar = new Hashtable<String, Object>();
        jar.put("name", artifact.substring(0, artifact.lastIndexOf(".")));
        jar.put("ext", artifact.substring(artifact.lastIndexOf(".") + 1));
        jar.put("meta.symbolicName", "zutubi.publication");
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

        Hashtable<String, Object> dependency = new Hashtable<String, Object>();
        dependency.put("project", "projects/" + projectDependency.project.name);
        dependency.put("revision", projectDependency.revision);
        dependency.put("stages", projectDependency.stage);
        dependency.put("transitive", projectDependency.transitive);
        dependency.put("meta.symbolicName", "zutubi.dependency");
        dependencies.add(dependency);

        xmlRpcHelper.saveConfig(projectDependenciesPath, projectDependencies, true);
    }

    private void assertIvyInRepository(String projectName, int buildNumber) throws IOException
    {
        assertInRepository(projectName + "/ivy-" + buildNumber + ".xml");
    }

    private void assertIvyNotInRepository(String projectName, int buildNumber) throws IOException
    {
        assertNotInRepository(projectName + "/ivy-" + buildNumber + ".xml");
    }

    private void assertJarInRepository(String projectName, String stageName, String artifactName, int buildNumber) throws IOException
    {
        assertInRepository(projectName + "/" + stageName + "/jars/" + artifactName + "-" + buildNumber + ".jar");
    }

    private void assertJarNotInRepository(String projectName, String stageName, String artifactName, int buildNumber) throws IOException
    {
        assertNotInRepository(projectName + "/" + stageName + "/jars/" + artifactName + "-" + buildNumber + ".jar");
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
        private List<Dependency> dependencies = new LinkedList<Dependency>();
        private List<String> artifacts = new LinkedList<String>();
        private List<Stage> stages = new LinkedList<Stage>();
        private Stage defaultStage = new Stage("default");

        private String publicationPattern = "build/[artifact].[ext]";

        private String retrievalPattern = "lib/[artifact].[ext]";

        private Project(String name)
        {
            this.name = name;
            this.stages.add(defaultStage);
        }

        private Project(String name, String org)
        {
            this(name);
            this.org = org;
        }

        private void addDependency(Project dependency)
        {
            dependencies.add(new Dependency(dependency));
        }

        private void addDependency(Project dependency, boolean transitive)
        {
            dependencies.add(new Dependency(dependency, transitive));
        }

        private void addDependency(Project dependency, boolean transitive, String stage)
        {
            dependencies.add(new Dependency(dependency, transitive, stage));
        }

        private void addDependency(Dependency dependnecy)
        {
            dependencies.add(dependnecy);
        }

        public Stage getDefaultStage()
        {
            return defaultStage;
        }

        private void addArtifact(String artifact)
        {
            artifacts.add(artifact);
        }

        private void addArtifacts(String... artifacts)
        {
            this.artifacts.addAll(Arrays.asList(artifacts));
        }

        private void addArtifact(String stageName, String artifactName)
        {
            getStage(stageName).addArtifact(artifactName);
        }

        private Stage getStage(final String name)
        {
            Stage stage = CollectionUtils.find(stages, new Predicate<Stage>()
            {
                public boolean satisfied(Stage stage)
                {
                    return stage.name.equals(name);
                }
            });

            if (stage == null)
            {
                stage = new Stage(name);
                stages.add(stage);
            }
            return stage;
        }

        public void setRetrievalPattern(String retrievalPattern)
        {
            this.retrievalPattern = retrievalPattern;
        }

        public void setPublicationPattern(String publicationPattern)
        {
            this.publicationPattern = publicationPattern;
        }
    }

    private class Dependency
    {
        private Project project;

        private boolean transitive = true;

        private String stage = "*";

        private String revision = "latest.integration";

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

        public Project getProject()
        {
            return project;
        }
    }

    private class Stage
    {
        private String name;

        private List<String> artifacts = new LinkedList<String>();

        private Map<String, String> properties = new HashMap<String, String>();

        private Stage(String name)
        {
            this.name = name;
        }

        private void addProperty(String name, String value)
        {
            properties.put(name, value);
        }

        private void addArtifact(String name)
        {
            artifacts.add(name);
        }

        public void addArtifacts(String... names)
        {
            artifacts.addAll(Arrays.asList(names));
        }
    }

    /**
     * Contains the configuration details to be passed through to the ant build
     * to a) produce the specified artifacts for each stage and b) to assert the
     * existance of the specified artifacts
     */
    private class Build
    {
        private List<String> artifacts = new LinkedList<String>();
        private List<String> dependencies = new LinkedList<String>();
        private List<String> notExpected = new LinkedList<String>();

        private int buildNumber;

        public void addArtifact(String artifact)
        {
            artifacts.add(artifact);
        }

        public void addArtifacts(String... artifacts)
        {
            this.artifacts.addAll(Arrays.asList(artifacts));
        }

        public void addDependency(String dependency)
        {
            dependencies.add(dependency);
        }

        public void addDependencies(String... dependencies)
        {
            this.dependencies.addAll(Arrays.asList(dependencies));
        }

        public void addNotExpected(String file)
        {
            this.notExpected.add(file);
        }

        public String getArtifactList()
        {
            return StringUtils.join(",", artifacts);
        }

        public String getDependencyList()
        {
            return StringUtils.join(",", dependencies);
        }

        public String getNotExpectedList()
        {
            return StringUtils.join(",", notExpected);
        }

        public int getBuildNumber()
        {
            return buildNumber;
        }

        public void setBuildNumber(int buildNumber)
        {
            this.buildNumber = buildNumber;
        }
    }
}
