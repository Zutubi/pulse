package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.utils.Repository;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Predicate;
import com.zutubi.util.RandomUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Hashtable;
import java.util.Vector;

/**
 * A set of acceptance tests for the build version functionality.  This includes
 * allowing a user to customise a builds version via prompts, configuration and
 * remote api.
 */
public class BuildVersionAcceptanceTest extends AcceptanceTestBase
{
    private String projectName;
    private Repository repository;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        
        // create a project.
        projectName = getName() + "-" + RandomUtils.randomString(10);
        rpcClient.RemoteApi.insertSimpleProject(projectName);

        repository = new Repository();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();

        super.tearDown();
    }

    public void testDefaultBuildVersionIsBuildNumber() throws Exception
    {
        // trigger a successful build.
        int buildNumber = rpcClient.RemoteApi.runBuild(projectName);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectName, buildNumber));

        assertBuildVersion(projectName, buildNumber, Integer.toString(buildNumber));
    }

    public void testSpecifyFixedBuildVersion() throws Exception
    {
        String buildVersion = "FIXED";

        int buildNumber = rpcClient.RemoteApi.runBuild(projectName, asPair("version", (Object)buildVersion));
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectName, buildNumber));

        assertBuildVersion(projectName, buildNumber, buildVersion);
    }

    public void testSpecifyVariableBuildVersion() throws Exception
    {
        String buildVersion = "${project}-${build.number}";

        int buildNumber = rpcClient.RemoteApi.runBuild(projectName, asPair("version", (Object)buildVersion));
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectName, buildNumber));

        assertBuildVersion(projectName, buildNumber, projectName + "-" + buildNumber);
    }

    public void testBuildRevisionInBuildVersion() throws Exception
    {
        String buildVersion = "version-$(build.revision)";

        int buildNumber = rpcClient.RemoteApi.runBuild(projectName, asPair("version", (Object)buildVersion));
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectName, buildNumber));

        String revision = rpcClient.RemoteApi.getBuildRevision(projectName, buildNumber);
        assertBuildVersion(projectName, buildNumber, "version-" + revision);
    }

    public void testSpecifyVersionViaManualPrompt() throws Exception
    {
        rpcClient.loginAsAdmin();

        // edit the build options, setting prompt to true.
        rpcClient.RemoteApi.enableBuildPrompting(projectName);

        getBrowser().loginAsAdmin();

        // trigger a build
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, projectName);
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        TriggerBuildForm form = getBrowser().createForm(TriggerBuildForm.class);
        form.waitFor();
        assertTrue(form.isFormPresent());

        // leave the revision blank
        form.triggerFormElements(asPair("version", "OH_HAI"));

        rpcClient.RemoteApi.waitForBuildToComplete(projectName, 1);

        assertBuildVersion(projectName, 1, "OH_HAI");
    }

    /**
     * Verify that the build version is as expected.
     *
     * @param projectName       the project to which the build belongs
     * @param buildNumber       the unique build identifier
     * @param expectedVersion   the expected version string
     *
     * @throws Exception on error.
     */
    private void assertBuildVersion(String projectName, int buildNumber, String expectedVersion) throws Exception
    {
        // verify a build version by:
        // a) the ivy file name - an exception is generated if it is not found or valid.
        repository.getIvyModuleDescriptor(projectName, expectedVersion);

        // b) the xml rpc interface - build details
        assertEquals(expectedVersion, rpcClient.RemoteApi.getBuildVersion(projectName, buildNumber));

        // c) the environment text contents
        String text = getEnvironmentText(projectName, buildNumber);

        assertThat(text, containsString("PULSE_BUILD_VERSION=" + expectedVersion));
    }

    private String getEnvironmentText(String projectName, int buildNumber) throws Exception
    {
        Vector<Hashtable<String, Object>> artifacts = rpcClient.RemoteApi.getArtifactsInBuild(projectName, buildNumber);
        Hashtable<String, Object> artifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return artifact.get("name").equals("environment");
            }
        });
        assertNotNull(artifact);

        String permalink = (String) artifact.get("permalink");
        return AcceptanceTestUtils.readUriContent(baseUrl + "/" + permalink.substring(1) + "env.txt");
    }
}
