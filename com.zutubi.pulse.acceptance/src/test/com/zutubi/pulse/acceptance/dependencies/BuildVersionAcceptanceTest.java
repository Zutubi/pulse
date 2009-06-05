package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.BaseXmlRpcAcceptanceTest;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.isInArtifactRepository;
import static com.zutubi.pulse.acceptance.dependencies.ArtifactRepositoryTestUtils.ivyPath;
import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
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
public class BuildVersionAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private String projectName;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        
        // create a project.
        projectName = getName() + "-" + RandomUtils.randomString(10);
        xmlRpcHelper.insertSimpleProject(projectName);
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();

        super.tearDown();
    }

    public void testDefaultBuildVersionIsBuildNumber() throws Exception
    {
        // trigger a successful build.
        int buildNumber = xmlRpcHelper.runBuild(projectName);
        assertTrue(isBuildSuccessful(projectName, buildNumber));

        assertBuildVersion(projectName, buildNumber, Integer.toString(buildNumber));
    }

    public void testSpecifyFixedBuildVersion() throws Exception
    {
        String buildVersion = "FIXED";

        int buildNumber = xmlRpcHelper.runBuild(projectName, asPair("version", (Object)buildVersion));
        assertTrue(isBuildSuccessful(projectName, buildNumber));

        assertBuildVersion(projectName, buildNumber, buildVersion);

        // regardless of the build version being used, we should always have
        // an ivy file for the build number.
        assertTrue(isInArtifactRepository(ivyPath(projectName, buildNumber)));
    }

    public void testSpecifyVariableBuildVersion() throws Exception
    {
        String buildVersion = "${project}-${build.number}";

        int buildNumber = xmlRpcHelper.runBuild(projectName, asPair("version", (Object)buildVersion));
        assertTrue(isBuildSuccessful(projectName, buildNumber));

        assertBuildVersion(projectName, buildNumber, projectName + "-" + buildNumber);

        // regardless of the build version being used, we should always have
        // an ivy file for the build number.
        assertTrue(isInArtifactRepository(ivyPath(projectName, buildNumber)));
    }

    public void testSpecifyVersionViaManualPrompt() throws Exception
    {
        loginAsAdmin();

        // edit the build options, setting prompt to true.
        xmlRpcHelper.enableBuildPrompting(projectName);

        SeleniumBrowser browser = new SeleniumBrowser();
        try
        {
            browser.start();
            
            LoginPage page = browser.openAndWaitFor(LoginPage.class);
            page.login("admin", "admin");

            // trigger a build
            ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, projectName);
            home.triggerBuild();

            // we should be prompted for a revision and a pname value.
            TriggerBuildForm form = browser.create(TriggerBuildForm.class);
            form.waitFor();
            assertTrue(form.isFormPresent());

            // leave the revision blank
            form.triggerFormElements(asPair("version", "OH_HAI"));

            browser.click("logout");
            browser.waitForPageToLoad();
        }
        finally
        {
            browser.stop();
        }

        xmlRpcHelper.waitForBuildToComplete(projectName, 1);

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
        // a) the ivy file name
        assertTrue(isInArtifactRepository(ivyPath(projectName, expectedVersion)));

        // b) the xml rpc interface - build details
        Hashtable<String, Object> buildDetails = xmlRpcHelper.getBuild(projectName, buildNumber);
        assertEquals(expectedVersion, buildDetails.get("version"));

        // c) the environment text contents
        String text = getEnvironmentText(projectName, buildNumber);

        assertThat(text, containsString("PULSE_BUILD_VERSION=" + expectedVersion));
    }

    private String getEnvironmentText(String projectName, int buildNumber) throws Exception
    {
        Vector<Hashtable<String, Object>> artifacts = xmlRpcHelper.getArtifactsInBuild(projectName, buildNumber);
        Hashtable<String, Object> artifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return artifact.get("name").equals("environment");
            }
        });
        assertNotNull(artifact);

        String permalink = (String) artifact.get("permalink");
        return downloadAsAdmin(baseUrl + permalink.substring(1) + "env.txt");
    }
}
