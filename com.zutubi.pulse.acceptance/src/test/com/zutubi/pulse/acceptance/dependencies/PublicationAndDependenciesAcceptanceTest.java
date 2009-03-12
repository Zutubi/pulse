package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.*;
import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;

import java.util.Hashtable;
import java.util.Vector;

public class PublicationAndDependenciesAcceptanceTest  extends BaseXmlRpcAcceptanceTest
{
    private static final int BUILD_TIMEOUT = 90000;
    private static final int AVAILABILITY_TIMEOUT = 10000;

    private String random = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        random = randomName();

        loginAsAdmin();

        clearArtifactRepository();
    }

    protected void tearDown() throws Exception
    {
        logout();

        super.tearDown();
    }

    public void testInternalPublishingOfProjectArtifactToRepository() throws Exception
    {
        String projectName = random + "A";

        insertProject(projectName, "artifacts", "-Dartifact.list=build/artifact.jar");

        addPublication(projectName, "default", "artifact", "jar");

        int buildNumber = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        // ensure that the build passed.
        assertTrue(isBuildSuccessful(projectName, buildNumber));

        // check that the expected artifacts are available.
        String artifact = projectName + "/default/jars/artifact-" + buildNumber + ".jar";
        assertTrue(waitUntilInRepository(artifact, AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(artifact + ".md5", AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(artifact + ".sha1", AVAILABILITY_TIMEOUT));

        // check that the expected ivy file is available.
        String ivy = projectName + "/ivy-" + buildNumber + ".xml";
        assertTrue(waitUntilInRepository(ivy, AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(ivy + ".md5", AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(ivy + ".sha1", AVAILABILITY_TIMEOUT));
    }

    public void testBuildFailsIfPublishingFailsToLocateArtifact() throws Exception
    {
        String projectName = random + "A";

        insertProject(projectName, "artifacts", "-Dartifact.list=build/artifact.jar");

        // configure the default stage.
        addPublication(projectName, "default", "artifact", "txt");

        int buildNumber = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        // ensure that the build passed.
        assertTrue(isBuildErrored(projectName, buildNumber));
    }

    public void testInternalRetrievalOfProjectDependenciesFromRepository() throws Exception
    {
        String projectName = random + "B";

        // part a) run a build to create the dependencies:
        testInternalPublishingOfProjectArtifactToRepository();

        insertProject(projectName, "dependencies", "-Ddependency.list=lib/artifact.jar");

        // configure the default stage.
        addDependency(projectName, random + "A", "*", "latest.integration");

        int buildNumber = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        // ensure that the build passed.
        assertTrue(isBuildSuccessful(projectName, buildNumber));

        // check that the expected ivy file is available.
        String ivy = projectName + "/ivy-" + buildNumber + ".xml";
        assertTrue(waitUntilInRepository(ivy, AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(ivy + ".md5", AVAILABILITY_TIMEOUT));
        assertTrue(waitUntilInRepository(ivy + ".sha1", AVAILABILITY_TIMEOUT));
    }

    private void addPublication(String projectName, String stageName, String name, String ext) throws Exception
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
        jar.put("name", name);
        jar.put("ext", ext);
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
        dependency.put("project", "projects/"+ dependentProject);
        dependency.put("revision", revision);
        dependency.put("stages", stages);
        dependency.put("meta.symbolicName", "zutubi.dependency");
        dependencies.add(dependency);

        xmlRpcHelper.saveConfig(projectDependenciesPath, projectDependencies, true);
    }

    private void setPublicationPattern(String stageName, String pattern) throws Exception
    {
        String stagePath = "projects/" + random + "/stages/" + stageName;
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put("publicationPattern", pattern);
        xmlRpcHelper.saveConfig(stagePath, stage, true);
    }

    private void insertProject(String projectName, String target, String args) throws Exception
    {
        Hashtable<String,Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("targets", target);
        antConfig.put("args", args);

        xmlRpcHelper.insertSingleCommandProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_ANT_REPOSITORY), antConfig);
    }
}
