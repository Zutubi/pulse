package com.zutubi.pulse.acceptance.dependencies;

import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.getAttribute;
import com.zutubi.pulse.core.dependency.ivy.DefaultIvyClientFactory;
import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.STATUS_INTEGRATION;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId;
import com.zutubi.pulse.master.dependency.ivy.ModuleDescriptorFactory;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.PublicationConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IvyIntegrationAcceptanceTest extends PulseTestCase
{
    private File tmp;
    private IvyClient core;
    private Repository repository;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        repository = new Repository(tmp);

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("repository.base", tmp.toURI().toString());

        String artifactPattern = repository.getArtifactPattern();
        String ivyPattern = repository.getIvyPattern();

        DefaultIvyClientFactory icf = new DefaultIvyClientFactory(artifactPattern, ivyPattern);
        core = icf.createClient(variables);
    }

    protected void tearDown() throws Exception
    {
        repository.clean();
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testPublishSingleArtifact() throws IOException, ParseException
    {
        ProjectConfiguration project = newProject("zutubi", "project");
        BuildStageConfiguration stage = addStage(project, "stage");
        addArtifact(stage, "artifact", "jar");

        runBuild(project, "1");

        assertTrue(repository.isFile("zutubi", "project", "1", "stage", "artifact", "jar"));
        assertTrue(repository.isIvyFile("zutubi", "project", "1"));

        assertIsFile("zutubi/project/stage/build/artifact.jar");
    }

    public void testPublishMultipleArtifacts() throws IOException, ParseException
    {
        ProjectConfiguration project = newProject("zutubi", "project");
        BuildStageConfiguration stage = addStage(project, "stage");
        addArtifact(stage, "artifact", "jar");
        addArtifact(stage, "another-artifact", "jar");

        runBuild(project, "1");

        assertIsFile("zutubi/project/stage/build/artifact.jar");

        assertTrue(repository.isFile("zutubi", "project", "1", "stage", "artifact", "jar"));
        assertTrue(repository.isFile("zutubi", "project", "1", "stage", "another-artifact", "jar"));
        assertTrue(repository.isIvyFile("zutubi", "project", "1"));
    }

    public void testPublishMultipleStages() throws IOException, ParseException
    {
        ProjectConfiguration project = newProject("zutubi", "project");
        BuildStageConfiguration stageA = addStage(project, "stageA");
        addArtifact(stageA, "artifact", "jar");
        BuildStageConfiguration stageB = addStage(project, "stageB");
        addArtifact(stageB, "artifact", "jar");

        runBuild(project, "1");

        assertIsFile("zutubi/project/stageA/build/artifact.jar");
        assertIsFile("zutubi/project/stageB/build/artifact.jar");

        assertTrue(repository.isFile("zutubi", "project", "1", "stageA", "artifact", "jar"));
        assertTrue(repository.isFile("zutubi", "project", "1", "stageB", "artifact", "jar"));
        assertTrue(repository.isIvyFile("zutubi", "project", "1"));
    }

    public void testRetrieveSingleArtifact() throws IOException, ParseException
    {
        // without a published ivy file, we need to rely on naming conventions to locate the artifact.
        // Note: can not include the stage name here since there is no published ivy file available for
        // the resolution mechanism to use to find out the stage name.
        createFile("repository/zutubi/projectA/jars/projectA-1.jar");

        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        BuildStageConfiguration stageA = addStage(projectA, "stageA");
        addArtifact(stageA, "projectA", "jar");

        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        addStage(projectB, "stageB");
        addDependency(projectB, projectA).setRevision("1");

        runBuild(projectB, "1");

        assertIsFile("zutubi/projectB/stageB/lib/projectA-1.jar");
    }

    public void testBuildDependencyAndDependent() throws IOException, ParseException
    {
        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        BuildStageConfiguration stageA = addStage(projectA, "stageA");
        addArtifact(stageA, "artifactA", "jar");
        addArtifact(stageA, "artifactB", "jar");
        BuildStageConfiguration stageB = addStage(projectA, "stageB");
        addArtifact(stageB, "artifactC", "jar");

        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        BuildStageConfiguration stageC = addStage(projectB, "stageC");
        addArtifact(stageC, "artifactD", "jar");

        addDependency(projectB, projectA);

        runBuild(projectA, "234");

        // verify the expected artifacts where created by the build.
        assertIsFile("zutubi/projectA/stageA/build/artifactA.jar");
        assertIsFile("zutubi/projectA/stageA/build/artifactB.jar");
        assertIsFile("zutubi/projectA/stageB/build/artifactC.jar");

        // verify that the expected artifacts where published to the repository.
        assertTrue(repository.isFile("zutubi", "projectA", "234", "stageA", "artifactA", "jar"));
        assertTrue(repository.isFile("zutubi", "projectA", "234", "stageA", "artifactB", "jar"));
        assertTrue(repository.isFile("zutubi", "projectA", "234", "stageB", "artifactC", "jar"));
        assertTrue(repository.isIvyFile("zutubi", "projectA", "234"));

        runBuild(projectB, "234");

        // verify that the expected files have been retrieved.
        assertIsFile("zutubi/projectB/stageC/lib/artifactA-234.jar");
        assertIsFile("zutubi/projectB/stageC/lib/artifactB-234.jar");
        assertIsFile("zutubi/projectB/stageC/lib/artifactC-234.jar");

        // verify that the expected project artifact was generated.
        assertIsFile("zutubi/projectB/stageC/build/artifactD.jar");

        // verify that the expected artifacts where published to the repository.
        assertTrue(repository.isFile("zutubi", "projectB", "234", "stageC", "artifactD", "jar"));
        assertTrue(repository.isIvyFile("zutubi", "projectB", "234"));
    }

    public void testTransitiveDependency() throws IOException, ParseException
    {
        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        addArtifact(addStage(projectA, "stageA"), "artifactA", "jar");
        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        addArtifact(addStage(projectB, "stageB"), "artifactB", "jar");
        addDependency(projectB, projectA);
        ProjectConfiguration projectC = newProject("zutubi", "projectC");
        addArtifact(addStage(projectC, "stageC"), "artifactC", "jar");
        addDependency(projectC, projectB);

        runBuild(projectA, "5");
        runBuild(projectB, "5");

        assertIsFile("zutubi/projectB/stageB/lib/artifactA-5.jar");
        assertIsNotFile("zutubi/projectB/stageB/lib/artifactB-5.jar");

        runBuild(projectC, "5");

        assertIsFile("zutubi/projectC/stageC/lib/artifactA-5.jar");
        assertIsFile("zutubi/projectC/stageC/lib/artifactB-5.jar");
    }

    public void testLatestIntegrationVersioning() throws IOException, ParseException
    {
        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        addArtifact(addStage(projectA, "stageA"), "artifactA", "jar");
        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        addArtifact(addStage(projectB, "stageB"), "artifactB", "jar");
        addDependency(projectB, projectA);

        runBuild(projectA, "1");
        runBuild(projectA, "2");

        runBuild(projectB, "2");

        assertIsFile("zutubi/projectB/stageB/lib/artifactA-2.jar");
    }

    public void testFixedVersioning() throws IOException, ParseException
    {
        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        addArtifact(addStage(projectA, "stageA"), "artifactA", "jar");
        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        addArtifact(addStage(projectB, "stageB"), "artifactB", "jar");
        addDependency(projectB, projectA).setRevision("1");

        runBuild(projectA, "1");
        runBuild(projectA, "2");
        runBuild(projectA, "3");

        runBuild(projectB, "2");

        assertIsFile("zutubi/projectB/stageB/lib/artifactA-1.jar");
    }

    public void testStageDependency() throws IOException, ParseException
    {
        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        addArtifact(addStage(projectA, "stageA"), "artifactA", "jar");
        addArtifact(addStage(projectA, "stageB"), "artifactB", "jar");
        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        addArtifact(addStage(projectB, "stageC"), "artifactC", "jar");
        DependencyConfiguration dependency = addDependency(projectB, projectA);
        dependency.setAllStages(false);
        dependency.getStages().add(projectA.getStage("stageA"));

        runBuild(projectA, "1");
        runBuild(projectB, "1");

        assertIsFile("zutubi/projectB/stageC/lib/artifactA-1.jar");
        assertIsNotFile("zutubi/projectB/stageC/lib/artifactB-1.jar");
    }

    public void testStageNameWithSpace() throws IOException, ParseException
    {
        assertStageNameIsOk("stage name");
    }

    public void testStageNameWithAtSymbol() throws IOException, ParseException
    {
        assertStageNameIsOk("stage@name");
    }

    public void testStageNameWithHash() throws IOException, ParseException
    {
        assertStageNameIsOk("stage#name");
    }

    public void testStageNameWithUnderscore() throws IOException, ParseException
    {
        assertStageNameIsOk("stage_name");
    }

    public void testStageNameWithEquals() throws IOException, ParseException
    {
        assertStageNameIsOk("stage=name");
    }

    public void testStageNameWithSemiColon() throws IOException, ParseException
    {
        // If un-encoded, this fails with: java.lang.IllegalStateException: bad ivy file
        // java.text.ParseException: Problem occured while parsing ivy file:
        // Configuration 'name' does not exist in module module:
        assertStageNameIsOk("stage;name");
    }

    public void testStageNameWithComma() throws IOException, ParseException
    {
        // If un-encoded, this fails with: java.lang.IllegalStateException: bad ivy file
        // java.text.ParseException: Problem occured while parsing ivy file:
        // Configuration 'name' does not exist in module module:
        assertStageNameIsOk("stage,name");
    }

    public void testStageNameWithStar() throws IOException, ParseException
    {
        // fails build on windows due to java.io.FileNotFoundException: C:\Documents and Settings\daniel\.ivy2\cache\zutubi-projectA-stage*name.xml
        // (The filename, directory name, or volume label syntax is incorrect)
        assertStageNameIsOk("stage*name");
    }

    public void testResolvingArtifacts() throws IOException, ParseException
    {
        ProjectConfiguration project = newProject("zutubi", "project");
        BuildStageConfiguration stage = addStage(project, "stage");
        addArtifact(stage, "artifact", "jar");

        String revision = "1";
        runBuild(project, revision);

        ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(project, revision);

        List<String> artifactPaths = core.getArtifactPaths(mrid);
        assertEquals(1, artifactPaths.size());
        assertTrue(repository.isFile(artifactPaths.get(0)));
    }

    public void testResolvingLatestArtifact() throws IOException, ParseException
    {
        ProjectConfiguration project = newProject("zutubi", "project");
        BuildStageConfiguration stage = addStage(project, "stage");
        addArtifact(stage, "artifact", "jar");

        runBuild(project, "1");
        runBuild(project, "2");
        runBuild(project, "3");

        ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(project, "latest.integration");

        List<String> artifactPaths = core.getArtifactPaths(mrid);
        assertEquals(1, artifactPaths.size());
        assertTrue(repository.isFile(artifactPaths.get(0)));
    }

    /**
     * When the revision is fixed for a build, we allow the same version to be used for multiple versions
     * of an artifact.  We need to ensure that the latest version of the artifact is what is actually in
     * the repository.
     *
     * @throws Exception on error.
     */
    public void testFixedRevisionPublicationsOverrideOlderPublications() throws Exception
    {
        ProjectConfiguration project = newProject("zutubi", "projectA");
        addArtifact(addStage(project, "stage"), "artifact", "txt");

        runBuild(project, "FIXED");
        String firstPublicationField = getAttribute("publication", repository.getIvyFile(project, "FIXED"));

        // sleep to ensure that there is no valid way for the publication fields to be
        // the same, regardless of the level of rounding.
        Thread.sleep(1000);

        runBuild(project, "FIXED");
        String secondPublicationField = getAttribute("publication", repository.getIvyFile(project, "FIXED"));

        assertFalse(secondPublicationField.compareTo(firstPublicationField) == 0);
    }

    private ProjectConfiguration newProject(String organisation, String name)
    {
        ProjectConfiguration project = new ProjectConfiguration();
        project.setName(name);
        project.setOrganisation(organisation);
        project.getDependencies().setStatus(STATUS_INTEGRATION);
        return project;
    }

    private BuildStageConfiguration addStage(ProjectConfiguration project, String stageName)
    {
        BuildStageConfiguration stage = new BuildStageConfiguration(stageName);
        project.getStages().put(stageName, stage);
        return stage;
    }

    private PublicationConfiguration addArtifact(BuildStageConfiguration stage, String name, String extension)
    {
        PublicationConfiguration publication = new PublicationConfiguration();
        publication.setName(name);
        publication.setExt(extension);
        stage.getPublications().add(publication);
        return publication;
    }

    private DependencyConfiguration addDependency(ProjectConfiguration project, ProjectConfiguration dependentProject)
    {
        DependencyConfiguration dependency = new DependencyConfiguration();
        dependency.setProject(dependentProject);
        project.getDependencies().getDependencies().add(dependency);
        return dependency;
    }

    private void assertStageNameIsOk(String stageName) throws IOException, ParseException
    {
        ProjectConfiguration projectA = newProject("zutubi", "projectA");
        BuildStageConfiguration projectAStage = addStage(projectA, stageName);
        addArtifact(projectAStage, "artifactA", "jar");

        runBuild(projectA, "1");
        assertTrue(repository.isFile("zutubi", "projectA", "1", stageName, "artifactA", "jar"));

        ProjectConfiguration projectB = newProject("zutubi", "projectB");
        addArtifact(addStage(projectB, "stageB"), "artifactB", "jar");
        addDependency(projectB, projectA).getStages().add(projectAStage);

        runBuild(projectB, "1");
        assertIsFile("zutubi/projectB/stageB/lib/artifactA-1.jar"); // THIS IS THE IMPORTANT PART.
        assertTrue(repository.isFile("zutubi", "projectB", "1", "stageB", "artifactB", "jar"));
    }

    private void runBuild(ProjectConfiguration project, String revision) throws IOException, ParseException
    {
        // Resovle and deliver this projects full ivy.xml, then use it to retrieve the dependencies.
        ModuleDescriptor descriptor = createModuleDescriptor(project, null);
        ResolveReport resolveReport = core.resolve(descriptor);
        if (resolveReport.hasError())
        {
            @SuppressWarnings({"unchecked"})
            List<String> problemMessages = resolveReport.getAllProblemMessages();
            fail("Resolve failed:\n" + StringUtils.join("\n", problemMessages));
        }

        // publish artifacts as they are generated.
        for (BuildStageConfiguration stage : project.getStages().values())
        {
            // lib path is independent of IVY.
            File lib = new File(tmp, PathUtils.getPath(project.getOrganisation(), project.getName(), IvyUtils.ivyEncodeStageName(stage.getName()), "lib"));
            core.retrieve(descriptor.getModuleRevisionId(), lib.getAbsolutePath() + "/[artifact]-[revision].[ext]");

            // 'run the build'
            // build path is independent of IVY. (however, not independent of file system issues).
            File baseDir = new File(tmp, PathUtils.getPath(project.getOrganisation(), project.getName(), IvyUtils.ivyEncodeStageName(stage.getName()), "build"));
            for (PublicationConfiguration artifact : stage.getPublications())
            {
                createArtifact(baseDir, artifact);
            }

            Map<String, String> extraAttributes = new HashMap<String, String>();
            extraAttributes.put(MasterIvyModuleRevisionId.EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stage.getName()));  // e:stage is used for generating publication path so needs appropriate modification.
            ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(project.getOrganisation(), project.getName(), null, extraAttributes);

            // publish the artifacts from each stage.
            for (PublicationConfiguration artifact : stage.getPublications())
            {
                core.publish(mrid, revision, IvyUtils.ivyEncodeStageName(stage.getName()), baseDir.getAbsolutePath() + "/" + artifact.getName() + "." + artifact.getExt());
            }
        }

        // 'commit' the build by delivering the final ivy file.
        core.publishIvy(descriptor, revision);
    }

    private DefaultModuleDescriptor createModuleDescriptor(ProjectConfiguration project, String revision)
    {
        ModuleDescriptorFactory f = new ModuleDescriptorFactory();
        return f.createDescriptor(project, revision);
    }

    private void assertIsFile(String filePath)
    {
        if (!isFile(filePath))
        {
            System.out.println("Failed to locate: " + filePath);
            listTmp();
        }
        assertTrue(isFile(filePath));
    }

    private void listTmp()
    {
        System.out.println("Listing:");
        String prefix = "";
        list(tmp, prefix);
    }

    private void list(File base, String prefix)
    {
        for (File f : base.listFiles())
        {
            System.out.println(prefix + f.getName());
            if (f.isDirectory())
            {
                list(f, prefix + "  ");
            }
        }
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

    private void createArtifact(File base, PublicationConfiguration artifact) throws IOException
    {
        File f = new File(base, artifact.getName() + "." + artifact.getExt());
        assertTrue(f.getParentFile().exists() || f.getParentFile().mkdirs());
        assertTrue(f.exists() || f.createNewFile());
    }

    private static class Repository
    {
        private String ivyPattern = "repository/([organisation]/)[module]/([stage]/)ivy-[revision].xml";
        private String artifactPattern = "repository/([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[type]";

        private File baseDir;

        private Repository(File baseDir)
        {
            this.baseDir = baseDir;
        }

        public void clean() throws IOException
        {
            removeDirectory(baseDir);
            if (!baseDir.mkdirs())
            {
                throw new IOException("Failed to recreate: " + baseDir.getAbsolutePath());
            }
        }

        public String getIvyPattern()
        {
            return ivyPattern;
        }

        public String getArtifactPattern()
        {
            return artifactPattern;
        }

        public void createFile(ModuleRevisionId mrid) throws IOException
        {
            String path = IvyPatternHelper.substitute(artifactPattern, mrid);

            File f = new File(baseDir, path);
            assertTrue(f.getParentFile().exists() || f.getParentFile().mkdirs());
            assertTrue(f.exists() || f.createNewFile());
        }

        public boolean isFile(String relativePath)
        {
            return new File(baseDir, relativePath).isFile();
        }

        public boolean isFile(String org, String name, String revision, String stageName, String artifactName, String artifactExt)
        {
            Map<String, String> extraAttributes = new HashMap<String, String>();
            extraAttributes.put(MasterIvyModuleRevisionId.EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stageName));
            ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(org, name, revision, extraAttributes);

            String path = IvyPatternHelper.substitute(artifactPattern, mrid, artifactName, artifactExt, artifactExt);
            return new File(baseDir, path).isFile();
        }

        public File getIvyFile(ProjectConfiguration project, String revision)
        {
            ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(project, revision);
            String path = IvyPatternHelper.substitute(ivyPattern, mrid);
            return new File(baseDir, path);
        }

        public boolean isIvyFile(String org, String name, String revision)
        {
            ModuleRevisionId mrid = MasterIvyModuleRevisionId.newInstance(org, name, null, revision);
            String path = IvyPatternHelper.substitute(ivyPattern, mrid);
            return new File(baseDir, path).isFile();
        }
    }
}
