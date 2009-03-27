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

        int buildNumber = triggerSuccessfulBuild(project);

        // verify existance of expected artifacts.
        assertIvyInRepository(project.name, buildNumber);
    }

    public void testPublish_SingleArtifact() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifact("artifact.jar");
        createProject(project);

        int buildNumber = triggerSuccessfulBuild(project);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublish_MultipleArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifact("artifact.jar");
        project.addArtifact("another-artifact.jar");
        createProject(project);

        int buildNumber = triggerSuccessfulBuild(project);

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

        int buildNumber = triggerSuccessfulBuild(project);

        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, "default", "artifact", buildNumber);
        assertJarInRepository(project.name, "stage", "artifact", buildNumber);
    }

    public void testPublish_CustomPublicationPattern() throws Exception
    {
        Project project = new Project(randomName());
        project.setPublicationPattern("my/build/[artifact].[ext]");
        project.addArtifact("artifact.jar");
        createProject(project);

        int buildNumber = triggerSuccessfulBuild(project);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublish_CustomPublicationPatternViaProperty() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifact("artifact.jar");
        createProject(project);

        // Need to set this separately so that the artifacts are configured correctly
        // via createProject
        setPublicationPattern(project.name, "${publicationPattern}");
        xmlRpcHelper.insertStageProperty(project.name, project.defaultStage.name, "publicationPattern", "build/[artifact].[ext]");

        int buildNumber = triggerSuccessfulBuild(project);

        // ensure that we have the expected artifact in the repository.
        assertIvyInRepository(project.name, buildNumber);
        assertJarInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testPublishFails_MissingArtifacts() throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifact("artifact.jar");
        createProject(project);

        // change the publication pattern so that the artifacts are searched for in the wrong location.
        setPublicationPattern(project.name, "invalid/path/[artifact].[ext]");

        int buildNumber = triggerBuild(project);
        assertTrue(isBuildErrored(project.name, buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(project.name, buildNumber);
        assertJarNotInRepository(project.name, project.defaultStage.name, "artifact", buildNumber);
    }

    public void testRetrieve_SingleArtifact() throws Exception
    {
        Project projectA = createAndTriggerProject("artifact.jar");

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        int buildNumber = triggerSuccessfulBuild(projectB);

        assertIvyInRepository(projectB.name, buildNumber);
    }

    public void testRetrieve_MultipleArtifacts() throws Exception
    {
        Project projectA = createAndTriggerProject("artifact.jar", "another-artifact.jar");

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        int buildNumber = triggerSuccessfulBuild(projectB);

        assertIvyInRepository(projectB.name, buildNumber);
    }

    public void testRetrieve_MultipleProjects() throws Exception
    {
        Project projectA = createAndTriggerProject("projectA-artifact.jar");
        Project projectB = createAndTriggerProject("projectB-artifact.jar");

        Project projectC = new Project(randomName());
        projectC.addDependency(projectA);
        projectC.addDependency(projectB);
        createProject(projectC);

        int buildNumber = triggerSuccessfulBuild(projectC);
        assertIvyInRepository(projectB.name, buildNumber);
    }

    public void testRetrieve_TransitiveDependencies() throws Exception
    {
        Project projectA = createAndTriggerProject("projectA-artifact.jar");

        Project projectB = new Project(randomName());
        projectB.addArtifact("projectB-artifact.jar");
        projectB.addDependency(projectA);
        createAndTriggerProject(projectB);

        Project projectC = new Project(randomName());
        projectC.addDependency(projectB);
        createProject(projectC);

        int buildNumber = triggerSuccessfulBuild(projectC);
        assertIvyInRepository(projectB.name, buildNumber);
    }

    public void testRetrieveFails_MissingDependencies() throws Exception
    {
        Project projectA = createAndTriggerProject("artifact.jar");

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        // change the retrieval pattern so that the artifacts are delivered to somewhere other than expected by the build.
        setRetrievalPattern(projectB.name, "some/other/path/[artifact].[ext]");

        int buildNumber = triggerBuild(projectB);
        assertTrue(isBuildFailure(projectB.name, buildNumber));

        // ensure that we have the expected artifact in the repository.
        assertIvyNotInRepository(projectB.name, buildNumber);
        assertJarNotInRepository(projectB.name, projectB.defaultStage.name, "artifact", buildNumber);
    }

    public void testDependentBuild_TriggeredOnSuccess() throws Exception
    {
        Project projectA = new Project(randomName());
        projectA.addArtifact("artifact.jar");
        createProject(projectA);

        Project projectB = new Project(randomName());
        projectB.addDependency(projectA);
        createProject(projectB);

        triggerSuccessfulBuild(projectA);

        xmlRpcHelper.waitForBuildToComplete(projectB.name, 1, BUILD_TIMEOUT);
    }

    public void testRepositoryFormat_OrgSpecified() throws Exception
    {
        Project project = new Project(randomName(), "org");
        project.addArtifact("artifact.jar");
        createProject(project);

        int buildNumber = triggerSuccessfulBuild(project);

        assertInRepository(project.org + "/" + project.name + "/ivy-" + buildNumber + ".xml");
    }

    private Project createAndTriggerProject(String... artifacts) throws Exception
    {
        Project project = new Project(randomName());
        project.addArtifacts(artifacts);
        createProject(project);

        triggerSuccessfulBuild(project);
        return project;
    }

    private Project createAndTriggerProject(Project project) throws Exception
    {
        createProject(project);
        triggerSuccessfulBuild(project);
        return project;
    }

    private int triggerSuccessfulBuild(Project project) throws Exception
    {
        int buildNumber = triggerBuild(project);
        assertTrue(isBuildSuccessful(project.name, buildNumber));
        return buildNumber;
    }

    private int triggerBuild(Project project) throws Exception
    {
        return xmlRpcHelper.runBuild(project.name, BUILD_TIMEOUT);
    }

    private void createProject(Project p) throws Exception
    {
        String target = "artifacts";
        if (p.dependencies.size() > 0)
        {
            target = "dependencies artifacts";
        }

        String args = "-Dartifact.list=\"${artifact.list}\"";

        if (p.dependencies.size() > 0)
        {
            // get the list of expected dependent artifacts.
            List<String> dependencies = applyIvyPattern(p.retrievalPattern, p.getTransitiveDependencies());
            args = args + " -Ddependency.list=\"" + StringUtils.join(",", dependencies) + "\"";
        }

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(p.name, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);

        if (TextUtils.stringSet(p.org))
        {
            setProjectOrganisation(p);
        }

        setPublicationPattern(p.name, p.publicationPattern);

        for (Stage stage : p.stages)
        {
            // create stage.
            ensureStageExists(p.name, stage.name);

            for (String artifact : stage.artifacts)
            {
                addPublication(p.name, stage.name, artifact);
            }

            List<String> artifacts = applyIvyPattern(p.publicationPattern, stage.artifacts);
            xmlRpcHelper.insertStageProperty(p.name, stage.name, "artifact.list", StringUtils.join(",", artifacts));
        }

        for (Project dependency : p.dependencies)
        {
            addDependency(p.name, dependency.name, "*", "latest.integration");
        }
    }

    private void setProjectOrganisation(Project p) throws Exception
    {
        String path = "projects/" + p.name;
        Hashtable<String, Object> projectConfig = xmlRpcHelper.getConfig(path);
        projectConfig.put("organisation", p.org);

        xmlRpcHelper.saveConfig(path, projectConfig, true);
    }

    private List<String> applyIvyPattern(final String pattern, final List<String> artifacts)
    {
        return CollectionUtils.map(artifacts, new Mapping<String, String>()
        {
            public String map(String s)
            {
                String name = s.substring(0, s.lastIndexOf("."));
                String ext = s.substring(s.lastIndexOf(".") + 1);
                return pattern.replace("[artifact]", name).replace("[ext]", ext);
            }
        });
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

    public void setPublicationPattern(String projectName, String publicationPattern) throws Exception
    {
        // configure the default stage.
        String dependenciesPath = "projects/" + projectName + "/dependencies";
        Hashtable<String, Object> dependencies = xmlRpcHelper.getConfig(dependenciesPath);
        dependencies.put("publicationPattern", publicationPattern);

        xmlRpcHelper.saveConfig(dependenciesPath, dependencies, true);
    }

    public void setRetrievalPattern(String projectName, String retrievalPattern) throws Exception
    {
        // configure the default stage.
        String dependenciesPath = "projects/" + projectName + "/dependencies";
        Hashtable<String, Object> dependencies = xmlRpcHelper.getConfig(dependenciesPath);
        dependencies.put("retrievalPattern", retrievalPattern);

        xmlRpcHelper.saveConfig(dependenciesPath, dependencies, true);
    }

    private void addPublication(String projectName, String stageName, String artifact) throws Exception
    {
        // configure the default stage.
        String stagePath = "projects/" + projectName + "/stages/" + stageName;
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        if (!stage.containsKey("publications"))
        {
            stage.put("publications", new Vector<Hashtable<String, Object>>());
        }
        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> publications = (Vector<Hashtable<String, Object>>) stage.get("publications");

        Hashtable<String, Object> jar = new Hashtable<String, Object>();
        jar.put("name", artifact.substring(0, artifact.lastIndexOf(".")));
        jar.put("ext", artifact.substring(artifact.lastIndexOf(".") + 1));
        jar.put("meta.symbolicName", "zutubi.publication");
        publications.add(jar);

        xmlRpcHelper.saveConfig(stagePath, stage, true);
    }

    private void addDependency(String projectName, String dependentProject, String stages, String revision) throws Exception
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
        dependency.put("project", "projects/" + dependentProject);
        dependency.put("revision", revision);
        dependency.put("stages", stages);
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

    private class Project
    {
        private String name;
        private String org;

        private List<Project> dependencies = new LinkedList<Project>();
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
            dependencies.add(dependency);
        }

        private void addArtifact(String name)
        {
            defaultStage.addArtifact(name);
        }

        private void addArtifacts(String... names)
        {
            defaultStage.addArtifacts(names);
        }

        private void addArtifact(String stageName, String artifactName)
        {
            Stage stage = getStage(stageName);
            if (stage == null)
            {
                stage = new Stage(stageName);
                stages.add(stage);
            }
            stage.addArtifact(artifactName);
        }

        private Stage getStage(final String name)
        {
            return CollectionUtils.find(stages, new Predicate<Stage>()
            {
                public boolean satisfied(Stage stage)
                {
                    return stage.name.equals(name);
                }
            });
        }

        public void setRetrievalPattern(String retrievalPattern)
        {
            this.retrievalPattern = retrievalPattern;
        }

        public void setPublicationPattern(String publicationPattern)
        {
            this.publicationPattern = publicationPattern;
        }

        public List<String> getAllArtifacts()
        {
            Set<String> artifacts = new HashSet<String>();
            for (Stage stage : stages)
            {
                artifacts.addAll(stage.artifacts);
            }
            return new LinkedList<String>(artifacts);
        }

        public List<String> getTransitiveDependencies()
        {
            List<String> allDependencies = new LinkedList<String>();
            for (Project dependency : dependencies)
            {
                for (Stage stage : dependency.stages)
                {
                    allDependencies.addAll(stage.artifacts);
                }
                if (dependency.dependencies.size() > 0)
                {
                    allDependencies.addAll(dependency.getTransitiveDependencies());
                }
            }
            return allDependencies;
        }
    }

    private class Stage
    {
        private String name;

        private List<String> artifacts = new LinkedList<String>();

        private Stage(String name)
        {
            this.name = name;
        }

        private void addArtifact(String name)
        {
            artifacts.add(name);
        }

        public void addArtifacts(String[] names)
        {
            artifacts.addAll(Arrays.asList(names));
        }
    }
}
