package com.zutubi.pulse.acceptance.dependencies;

import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.*;
import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.master.model.ProjectManager;

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
        logout();
        
        super.tearDown();
    }

    public void testIvyCanPublishToRepository() throws Exception
    {
        assertTrue(isNotInArtifactRepository("zutubi/com.zutubi.sample/jars"));

        // run the ivyant build, verify that a new artifact is added to the repository.
        int buildNumber = createAndRunIvyAntProject("publish");

        assertTrue(isInArtifactRepository("zutubi/com.zutubi.sample/jars"));
        
        // ensure that the build passed.
        assertTrue(isBuildSuccessful(random, buildNumber));
    }

    public void testIvyCanRetrieveFromRepository() throws Exception
    {
        // create the expected artifact file.
        createArtifactFile("zutubi/artifact/jars/artifact-1.0.0.jar");
        
        // artifact/jars/artifact-1.0.0.jar
        int buildNumber = createAndRunIvyAntProject("retrieve");

        // ensure that the build passed.
        assertTrue(isBuildSuccessful(random, buildNumber));
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
}
