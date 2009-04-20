package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.STATUS_INTEGRATION;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.tove.type.record.PathUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import org.apache.ivy.Ivy;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.core.settings.IvyVariableContainer;
import org.apache.ivy.core.settings.IvyVariableContainerImpl;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.IvyPatternHelper;

public class IvySupportTest extends PulseTestCase
{
    private File tmp;
    private IvySupport core;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        IvyVariableContainer variables = new IvyVariableContainerImpl();
        variables.setVariable("repository.base", tmp.getAbsolutePath(), true);

        IvySettings settings = new IvySettings(variables);
        settings.load(IvyManager.class.getResource("ivysettings.xml"));

        String artifactPattern = "${repository.base}/repository/[organisation]/[module]/([stage]/)[type]s/[artifact]-[revision].[type]";
        String ivyPattern = "${repository.base}/repository/[organisation]/[module]/([stage]/)ivy-[revision].xml";

        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("pulse");
        resolver.addArtifactPattern(IvyPatternHelper.substituteVariables(artifactPattern, variables));
        resolver.addIvyPattern(IvyPatternHelper.substituteVariables(ivyPattern, variables));
        resolver.setCheckmodified(true);

        settings.addResolver(resolver);
        settings.setDefaultResolver(resolver.getName());

        Ivy ivy = Ivy.newInstance(settings);
        core = new IvySupport(ivy);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        
        super.tearDown();
    }

    public void testPublishSingleArtifact() throws IOException, ParseException
    {
        Project project = new Project("zutubi", "project");
        Stage stage = project.addStage("stage");
        stage.addArtifact("artifact", "jar");

        runBuild(project, "1");

        assertIsFile("zutubi/project/stage/build/artifact.jar");
        assertIsFile("repository/zutubi/project/stage/jars/artifact-1.jar");
        assertIsFile("repository/zutubi/project/ivy-1.xml");
    }

    public void testPublishMultipleArtifacts() throws IOException, ParseException
    {
        Project project = new Project("zutubi", "project");
        Stage stage = project.addStage("stage");
        stage.addArtifact("artifact", "jar");
        stage.addArtifact("another-artifact", "jar");

        runBuild(project, "1");

        assertIsFile("zutubi/project/stage/build/artifact.jar");
        assertIsFile("repository/zutubi/project/stage/jars/artifact-1.jar");
        assertIsFile("repository/zutubi/project/stage/jars/another-artifact-1.jar");
        assertIsFile("repository/zutubi/project/ivy-1.xml");
    }

    public void testPublishMultipleStages() throws IOException, ParseException
    {
        Project project = new Project("zutubi", "project");
        Stage stageA = project.addStage("stageA");
        stageA.addArtifact("artifact", "jar");
        Stage stageB = project.addStage("stageB");
        stageB.addArtifact("artifact", "jar");

        runBuild(project, "1");

        assertIsFile("zutubi/project/stageA/build/artifact.jar");
        assertIsFile("zutubi/project/stageB/build/artifact.jar");

        assertIsFile("repository/zutubi/project/ivy-1.xml");
        assertIsFile("repository/zutubi/project/stageA/jars/artifact-1.jar");
        assertIsFile("repository/zutubi/project/stageB/jars/artifact-1.jar");
    }

    public void testRetrieveSingleArtifact() throws IOException, ParseException
    {
        // without a published ivy file, we need to rely on naming conventions to locate the artifact.
        // Note, we can no longer use the 'stage' variable.
        createFile("repository/zutubi/projectA/jars/projectA-1.jar");

        Project projectA = new Project("zutubi", "projectA");
        Stage stageA = projectA.addStage("stageA");
        stageA.addArtifact("projectA", "jar");

        Project projectB = new Project("zutubi", "projectB");
        projectB.addStage("stageB");
        projectB.addDependency(projectA);

        runBuild(projectB, "1");

        assertIsFile("projectB/stageB/lib/projectA-1.jar");
    }

    public void testBuildDependencyAndDependent() throws IOException, ParseException
    {
        Project projectA = new Project("zutubi", "projectA");
        Stage stageA = projectA.addStage("stageA");
        stageA.addArtifact("artifactA", "jar");
        stageA.addArtifact("artifactB", "jar");
        Stage stageB = projectA.addStage("stageB");
        stageB.addArtifact("artifactC", "jar");

        Project projectB = new Project("zutubi", "projectB");
        Stage stageC = projectB.addStage("stageC");
        stageC.addArtifact("artifactD", "jar");

        projectB.addDependency(projectA);

        runBuild(projectA, "234");

        // verify the expected artifacts where created by the build.
        assertIsFile("zutubi/projectA/stageA/build/artifactA.jar");
        assertIsFile("zutubi/projectA/stageA/build/artifactB.jar");
        assertIsFile("zutubi/projectA/stageB/build/artifactC.jar");

        // verify that the expected artifacts where published to the repository.
        assertIsFile("repository/zutubi/projectA/ivy-234.xml");
        assertIsFile("repository/zutubi/projectA/stageA/jars/artifactA-234.jar");
        assertIsFile("repository/zutubi/projectA/stageA/jars/artifactB-234.jar");
        assertIsFile("repository/zutubi/projectA/stageB/jars/artifactC-234.jar");

        runBuild(projectB, "234");

        // verify that the expected files have been retrieved.
        assertIsFile("projectB/stageC/lib/artifactA-234.jar");
        assertIsFile("projectB/stageC/lib/artifactB-234.jar");
        assertIsFile("projectB/stageC/lib/artifactC-234.jar");

        // verify that the expected project artifact was generated.
        assertIsFile("zutubi/projectB/stageC/build/artifactD.jar");

        // verify that the expected artifacts where published to the repository.
        assertIsFile("repository/zutubi/projectB/ivy-234.xml");
        assertIsFile("repository/zutubi/projectB/stageC/jars/artifactD-234.jar");
    }

    public void testTransitiveDependency() throws IOException, ParseException
    {
        Project projectA = new Project("zutubi", "projectA");
        projectA.addStage("stageA").addArtifact("artifactA", "jar");
        Project projectB = new Project("zutubi", "projectB");
        projectB.addStage("stageB").addArtifact("artifactB", "jar");
        projectB.addDependency(projectA);
        Project projectC = new Project("zutubi", "projectC");
        projectC.addStage("stageC").addArtifact("artifactC", "jar");
        projectC.addDependency(projectB);

        runBuild(projectA, "5");
        runBuild(projectB, "5");

        assertIsFile("projectB/stageB/lib/artifactA-5.jar");
        assertIsNotFile("projectB/stageB/lib/artifactB-5.jar");

        runBuild(projectC, "5");

        assertIsFile("projectC/stageC/lib/artifactA-5.jar");
        assertIsFile("projectC/stageC/lib/artifactB-5.jar");
    }

    public void testLatestIntegrationVersioning() throws IOException, ParseException
    {
        Project projectA = new Project("zutubi", "projectA");
        projectA.addStage("stageA").addArtifact("artifactA", "jar");
        Project projectB = new Project("zutubi", "projectB");
        projectB.addStage("stageB").addArtifact("artifactB", "jar");
        projectB.addDependency(projectA);

        runBuild(projectA, "1");
        runBuild(projectA, "2");

        runBuild(projectB, "2");

        assertIsFile("projectB/stageB/lib/artifactA-2.jar");
    }

    public void testFixedVersioning() throws IOException, ParseException
    {
        Project projectA = new Project("zutubi", "projectA");
        projectA.addStage("stageA").addArtifact("artifactA", "jar");
        Project projectB = new Project("zutubi", "projectB");
        projectB.addStage("stageB").addArtifact("artifactB", "jar");
        projectB.addRevisionDependency(projectA, "1");

        runBuild(projectA, "1");
        runBuild(projectA, "2");
        runBuild(projectA, "3");

        runBuild(projectB, "2");

        assertIsFile("projectB/stageB/lib/artifactA-1.jar");
    }

    public void testStageDependency() throws IOException, ParseException
    {
        Project projectA = new Project("zutubi", "projectA");
        projectA.addStage("stageA").addArtifact("artifactA", "jar");
        projectA.addStage("stageB").addArtifact("artifactB", "jar");
        Project projectB = new Project("zutubi", "projectB");
        projectB.addStage("stageC").addArtifact("artifactC", "jar");
        projectB.addStageDependency(projectA, "stageA");

        runBuild(projectA, "1");
        runBuild(projectB, "1");

        assertIsFile("projectB/stageC/lib/artifactA-1.jar");
        assertIsNotFile("projectB/stageC/lib/artifactB-1.jar");
    }

    // simulate a build for the specified project, generating, retrieving and publishing the
    // artifacts.
    private void runBuild(Project project, String revision) throws IOException, ParseException
    {
        // Resovle and deliver this projects full ivy.xml, then use it to retrieve the dependencies.
        ModuleDescriptor descriptor = createModuleDescriptor(project);
        core.resolve(descriptor);

        // publish artifacts as they are generated.
        for (Stage stage : project.getStages())
        {
            File lib = new File(tmp, PathUtils.getPath(project.getName(), stage.getName(), "lib"));
            core.retrieve(descriptor.getModuleRevisionId(), lib.getAbsolutePath() + "/[artifact]-[revision].[ext]");

            // 'run the build'
            File baseDir = new File(tmp, PathUtils.getPath(project.getOrg(), project.getName(), stage.getName(), "build"));
            for (Artifact artifact : stage.getArtifacts())
            {
                createArtifact(baseDir, artifact);
            }

            Map<String, String> extraAttributes = new HashMap<String, String>();
            extraAttributes.put("e:stage", stage.getName());
            ModuleRevisionId mrid = ModuleRevisionId.newInstance(project.getOrg(), project.getName(), null, extraAttributes);

            // publish the artifacts from each stage.
            for (Artifact artifact : stage.getArtifacts())
            {
                core.publish(mrid, revision, stage.getName(), baseDir.getAbsolutePath() + "/" + artifact.getPath());
            }
        }

        // 'commit' the build by delivering the final ivy file.
        core.deliver(descriptor.getModuleRevisionId(), revision);
    }

    private DefaultModuleDescriptor createModuleDescriptor(Project project)
    {
        ModuleRevisionId mrid = ModuleRevisionId.newInstance(project.getOrg(), project.getName(), null);

        DefaultModuleDescriptor descriptor = new DefaultModuleDescriptor(mrid, STATUS_INTEGRATION, null); // the status needs to be configurable - options include 'release'..
        descriptor.addConfiguration(new Configuration("build"));

        // setup the module dependencies.
        for (Dependency dependency : project.getDependencies())
        {
            Project dependentProject = dependency.getProject();

            ModuleRevisionId dependencyMrid = ModuleRevisionId.newInstance(dependentProject.getOrg(), dependentProject.getName(), dependency.getRevision());
            DefaultDependencyDescriptor depDesc = new DefaultDependencyDescriptor(dependencyMrid, true, false);
            if (TextUtils.stringSet(dependency.getStage()))
            {
                depDesc.addDependencyConfiguration("build", dependency.getStage()); // potentially a list of stages. '*' signals all, empty stage implies default.
            }
            descriptor.addDependency(depDesc);
        }

        // setup the module artifacts.
        for (Stage stage : project.getStages())
        {
            if (stage.getArtifacts().size() > 0)
            {
                descriptor.addConfiguration(new Configuration(stage.getName()));
                for (Artifact artifact : stage.getArtifacts())
                {
                    Map<String, String> extraAttributes = new HashMap<String, String>();
                    extraAttributes.put("e:stage", stage.getName());
                    MDArtifact ivyArtifact = new MDArtifact(descriptor, artifact.getName(), artifact.getType(), artifact.getType(), null, extraAttributes);
                    String conf = stage.getName();
                    ivyArtifact.addConfiguration(conf);
                    descriptor.addArtifact(conf, ivyArtifact);
                }
            }
        }

        return descriptor;
    }

    private void assertIsFile(String filePath)
    {
        assertTrue(isFile(filePath));
    }

    private void assertIsNotFile(String filePath)
    {
        assertFalse(isFile(filePath));
    }

    private boolean isFile(String filePath)
    {
        return new File(tmp, filePath).isFile();
    }

    private void createFile(String path) throws IOException
    {
        File f = new File(tmp, path);
        assertTrue(f.getParentFile().exists() || f.getParentFile().mkdirs());
        assertTrue(f.exists() || f.createNewFile());
    }

    private void createArtifact(File base, Artifact artifact) throws IOException
    {
        File f = new File(base, artifact.getName() + "." + artifact.getType());
        assertTrue(f.getParentFile().exists() || f.getParentFile().mkdirs());
        assertTrue(f.exists() || f.createNewFile());
    }

    private static class Artifact
    {
        private String name;
        private String type;
        private String path = "[artifact].[ext]";

        private Artifact(String name, String type)
        {
            this.name = name;
            this.type = type;
        }

        public String getPath()
        {
            return path;
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }
    }

    private static class Stage
    {
        private String name;
        private List<Artifact> artifacts = new LinkedList<Artifact>();

        public Stage(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public void addArtifact(Artifact a)
        {
            artifacts.add(a);
        }

        public Artifact addArtifact(String name, String type)
        {
            Artifact artifact = new Artifact(name, type);
            artifacts.add(artifact);
            return artifact;
        }

        public List<Artifact> getArtifacts()
        {
            return artifacts;
        }
    }

    private static class Project
    {
        private String name;
        private String org;
        private List<Stage> stages = new LinkedList<Stage>();
        private List<Dependency> dependencies = new LinkedList<Dependency>();

        public Project(String org, String name)
        {
            this.name = name;
            this.org = org;
        }

        public String getOrg()
        {
            return org;
        }

        public String getName()
        {
            return name;
        }

        public void addStage(Stage s)
        {
            stages.add(s);
        }

        public Stage addStage(String name)
        {
            Stage stage = new Stage(name);
            addStage(stage);
            return stage;
        }

        public List<Stage> getStages()
        {
            return stages;
        }

        public void addDependency(Project project)
        {
            dependencies.add(new Dependency(project));
        }

        public void addRevisionDependency(Project project, String revision)
        {
            dependencies.add(new Dependency(project, revision, null));
        }

        public void addStageDependency(Project project, String stage)
        {
            dependencies.add(new Dependency(project, null, stage));
        }

        public List<Dependency> getDependencies()
        {
            return dependencies;
        }
    }

    private static class Dependency
    {
        private Project project;
        private String revision = "latest.integration";
        private String stage = "*";

        public Dependency(Project project)
        {
            this.project = project;
        }

        public Dependency(Project project, String revision, String stage)
        {
            this.project = project;
            if (TextUtils.stringSet(stage))
            {
                this.stage = stage;
            }
            if (TextUtils.stringSet(revision))
            {
                this.revision = revision;
            }
        }

        public Project getProject()
        {
            return project;
        }

        public String getRevision()
        {
            return revision;
        }

        public String getStage()
        {
            return stage;
        }
    }

}
