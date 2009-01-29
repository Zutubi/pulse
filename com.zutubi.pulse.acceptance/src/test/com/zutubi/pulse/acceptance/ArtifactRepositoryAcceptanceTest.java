package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.FileSystemUtils;

import java.io.IOException;
import java.io.File;
import java.util.Hashtable;

public class ArtifactRepositoryAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final int BUILD_TIMEOUT = 90000;

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
        clearArtifactRepository();

        logout();
        
        super.tearDown();
    }

    public void testIvyCanPublishToRepository() throws Exception
    {
        assertNotInArtifactRepository("zutubi/com.zutubi.sample/jars");

        // run the ivyant build, verify that a new artifact is added to the repository.
        createAndRunIvyAntProject("publish");

        assertInArtifactRepository("zutubi/com.zutubi.sample/jars");
    }

    public void testIvyCanRetrieveFromRepository() throws Exception
    {
        // create the expected artifact file.
        createArtifactFile("zutubi/artifact/jars/artifact-1.0.0.jar");
        
        // artifact/jars/artifact-1.0.0.jar
        int buildNumber = createAndRunIvyAntProject("retrieve");

        // ensure that the build passed.
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(random, buildNumber);
        assertEquals("success", build.get("status"));
    }

    public void testMavenCanPublishToRepository()
    {
        // to be completed.
    }

    private int createAndRunIvyAntProject(String target) throws Exception
    {
        Hashtable<String,Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("target", target);
        
        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.IVY_ANT_REPOSITORY), antConfig);
        return xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
    }

    private void assertInArtifactRepository(String path) throws IOException
    {
        assertTrue(new File(getArtifactRepository(), path).exists());
    }

    private void assertNotInArtifactRepository(String path) throws IOException
    {
        assertFalse(new File(getArtifactRepository(), path).exists());
    }

    private void clearArtifactRepository() throws IOException
    {
        assertTrue(FileSystemUtils.rmdir(getArtifactRepository()));
        assertTrue(getArtifactRepository().mkdirs());
    }

    private void createArtifactFile(String path) throws IOException
    {
        File file = new File(getArtifactRepository(), path);
        assertTrue(file.getParentFile().mkdirs());
        assertTrue(file.createNewFile());
    }

    private File getArtifactRepository() throws IOException
    {
        return new File(AcceptanceTestUtils.getDataDirectory(), "repository");
    }

}
