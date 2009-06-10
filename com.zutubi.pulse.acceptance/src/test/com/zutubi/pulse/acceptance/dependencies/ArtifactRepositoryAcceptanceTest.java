package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.Constants;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.*;
import com.zutubi.pulse.master.model.ProjectManager;

import java.util.Hashtable;

public class ArtifactRepositoryAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
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

    // test that an external ivy process can publish to the internal artifact repository.
    public void testExternalIvyCanPublishToRepository() throws Exception
    {
        assertTrue(isNotInArtifactRepository("zutubi/com.zutubi.sample/jars"));

        // run the ivyant build, verify that a new artifact is added to the repository.
        int buildNumber = createAndRunIvyAntProject("publish");

        // ensure that the build passed.
        assertTrue(isBuildSuccessful(random, buildNumber));

        assertTrue(isInArtifactRepository("zutubi/com.zutubi.sample/jars"));
    }

    // test that an external ivy process can retrieve from the internal artifact repository.
    public void testExternalIvyCanRetrieveFromRepository() throws Exception
    {
        // create the expected artifact file.
        createArtifactFile("zutubi/artifact/jars/artifact-1.0.0.jar");
        
        // artifact/jars/artifact-1.0.0.jar
        int buildNumber = createAndRunIvyAntProject("retrieve");

        // ensure that the build passed.
        assertTrue(isBuildSuccessful(random, buildNumber));
    }

    private int createAndRunIvyAntProject(String target) throws Exception
    {
        Hashtable<String,Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put("targets", target);
        
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.IVY_ANT_REPOSITORY), antConfig);
        return xmlRpcHelper.runBuild(random);
    }
}
