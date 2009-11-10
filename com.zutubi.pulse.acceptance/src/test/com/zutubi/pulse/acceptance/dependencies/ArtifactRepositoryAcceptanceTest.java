package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;

import java.util.Hashtable;

public class ArtifactRepositoryAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private String random = null;
    private Repository repository = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        random = randomName();

        loginAsAdmin();

        repository = new Repository();
        repository.clean();
    }

    protected void tearDown() throws Exception
    {
        logout();
        
        super.tearDown();
    }

    // test that an external ivy process can publish to the internal artifact repository.
    public void testExternalIvyCanPublishToRepository() throws Exception
    {
        assertTrue(repository.isNotInRepository("zutubi/com.zutubi.sample/jars"));

        // run the ivyant build, verify that a new artifact is added to the repository.
        int buildNumber = createAndRunIvyAntProject("publish");

        // ensure that the build passed.
        assertEquals(ResultState.SUCCESS, getBuildStatus(random, buildNumber));

        assertTrue(repository.isInRepository("zutubi/com.zutubi.sample/jars"));
    }

    // test that an external ivy process can retrieve from the internal artifact repository.
    public void testExternalIvyCanRetrieveFromRepository() throws Exception
    {
        // create the expected artifact file.
        repository.createFile("zutubi/artifact/jars/artifact-1.0.0.jar");
        
        // artifact/jars/artifact-1.0.0.jar
        int buildNumber = createAndRunIvyAntProject("retrieve");

        // ensure that the build passed.
        assertEquals(ResultState.SUCCESS, getBuildStatus(random, buildNumber));
    }

    public void testExternalMavenCanUseRepository() throws Exception
    {
        String projectA = random + "A";
        int buildNumber = createAndRunMavenProject(projectA, "pom-artifact1.xml", "clean deploy");

        assertEquals(ResultState.SUCCESS, getBuildStatus(projectA, buildNumber));

        assertTrue(repository.isInRepository("zutubi/artifact1/maven-metadata.xml"));
        assertTrue(repository.isInRepository("zutubi/artifact1/1.0/artifact1-1.0.jar"));
        assertTrue(repository.isInRepository("zutubi/artifact1/1.0/artifact1-1.0.pom"));

        String projectB = random + "B";
        buildNumber = createAndRunMavenProject(projectB, "pom-artifact2.xml", "clean dependency:copy-dependencies");
        assertEquals(ResultState.SUCCESS, getBuildStatus(projectB, buildNumber));
    }

    private int createAndRunMavenProject(String projectName, String pom, String goals) throws Exception
    {
        Hashtable<String, Object> mavenConfig = xmlRpcHelper.createDefaultConfig(Maven2CommandConfiguration.class);
        mavenConfig.put("pomFile", pom);
        mavenConfig.put("settingsFile", "settings.xml");
        mavenConfig.put("goals", goals);

        xmlRpcHelper.insertSingleCommandProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.DEP_MAVEN_REPOSITORY), mavenConfig);
        return xmlRpcHelper.runBuild(projectName);
    }

    private int createAndRunIvyAntProject(String target) throws Exception
    {
        Hashtable<String,Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("targets", target);
        
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.IVY_ANT_REPOSITORY), antConfig);
        return xmlRpcHelper.runBuild(random);
    }
}
