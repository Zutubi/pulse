package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.util.RandomUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * @see com.zutubi.pulse.acceptance.PersonalBuildAcceptanceTest
 */
public class PersonalBuildCommandAcceptanceTest extends DevToolsTestBase
{
    private static final String PROMPT_SYNC_REQUIRED = "Plugins do not match the Pulse master.  Synchronise now?";
    private static final String OUTPUT_PATCH_ACCEPTED = "Patch accepted";
    private static final String OUTPUT_RESTARTING = "Restarting...";
    private static final String PATCH_TYPE_UNIFIED = "unified";
    private static final String TRIVIANT_REVISION = "28";

    private XmlRpcHelper xmlRpcHelper;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper = new XmlRpcHelper();
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testPluginSyncOnInstall() throws Exception
    {
        String output = setupAndSubmitPersonalBuild();

        assertThat(output, containsString(PROMPT_SYNC_REQUIRED));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString(OUTPUT_PATCH_ACCEPTED));
        
        xmlRpcHelper.waitForBuildToComplete(extractBuildNumber(output));
    }

    public void testNoSyncRequiredWhenPluginsInstalled() throws Exception
    {
        runPluginSync();

        String output = setupAndSubmitPersonalBuild();
        assertThat(output, not(containsString(PROMPT_SYNC_REQUIRED)));
        assertThat(output, not(containsString(OUTPUT_SYNC_COMPLETE)));
        assertThat(output, containsString(OUTPUT_PATCH_ACCEPTED));

        xmlRpcHelper.waitForBuildToComplete(extractBuildNumber(output));
    }

    public void testSyncWithRestart() throws Exception
    {
        runPluginSync();

        // Now install an extra plugin that requires a restart to remove.
        String pluginId = getName() + "." + RandomUtils.randomString(10);
        String pluginName = getName() + " " + RandomUtils.randomString(10);
        File pluginJar = AcceptanceTestUtils.createTestPlugin(tmpDir, pluginId, pluginName);
        assertTrue(pluginJar.renameTo(new File(devPaths.getPluginStorageDir(), pluginJar.getName())));

        String output = setupAndSubmitPersonalBuild();
        assertThat(output, containsString(PROMPT_SYNC_REQUIRED));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString(OUTPUT_RESTARTING));
        assertThat(output, containsString(OUTPUT_PATCH_ACCEPTED));

        xmlRpcHelper.waitForBuildToComplete(extractBuildNumber(output));
    }
    
    private String setupAndSubmitPersonalBuild() throws Exception
    {
        String projectName = getName() + RandomUtils.randomString(10);
        xmlRpcHelper.insertSimpleProject(projectName);
        File patchFile = copyInputToDirectory("patch", "txt", tmpDir);

        return runCommandWithInput(inputLines(YesNoResponse.YES.name()),
                COMMAND_PERSONAL,
                FLAG_SERVER, AcceptanceTestUtils.getPulseUrl(),
                FLAG_USER, ADMIN_CREDENTIALS.getUserName(),
                FLAG_PASSWORD, ADMIN_CREDENTIALS.getPassword(),
                FLAG_PROJECT, projectName,
                FLAG_REVISION, TRIVIANT_REVISION,
                FLAG_NO_UPDATE,
                FLAG_PATCH_FILE, patchFile.getName(),
                FLAG_PATCH_TYPE, PATCH_TYPE_UNIFIED
        );
    }

    private int extractBuildNumber(String output)
    {
        Pattern p = Pattern.compile("personal build ([0-9]+)");
        Matcher matcher = p.matcher(output);
        assertTrue(matcher.find());
        return Integer.parseInt(matcher.group(1));
    }

}
