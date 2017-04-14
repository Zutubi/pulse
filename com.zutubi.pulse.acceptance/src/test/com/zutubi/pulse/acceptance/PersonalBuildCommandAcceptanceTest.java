/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.rpc.RpcClient;
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

    private RpcClient rpcClient;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient = new RpcClient();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testPluginSyncOnInstall() throws Exception
    {
        String output = setupAndSubmitPersonalBuild();

        assertThat(output, containsString(PROMPT_SYNC_REQUIRED));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString(OUTPUT_PATCH_ACCEPTED));
        
        rpcClient.RemoteApi.waitForBuildToComplete(extractBuildNumber(output));
    }

    public void testNoSyncRequiredWhenPluginsInstalled() throws Exception
    {
        runPluginSync();

        String output = setupAndSubmitPersonalBuild();
        assertThat(output, not(containsString(PROMPT_SYNC_REQUIRED)));
        assertThat(output, not(containsString(OUTPUT_SYNC_COMPLETE)));
        assertThat(output, containsString(OUTPUT_PATCH_ACCEPTED));

        rpcClient.RemoteApi.waitForBuildToComplete(extractBuildNumber(output));
    }

    public void testSyncWithRestart() throws Exception
    {
        runPluginSync();

        // Now install an extra plugin that requires a restart to remove.
        String pluginId = getName() + "." + RandomUtils.insecureRandomString(10);
        String pluginName = getName() + " " + RandomUtils.insecureRandomString(10);
        File pluginJar = AcceptanceTestUtils.createTestPlugin(tmpDir, pluginId, pluginName);
        assertTrue(pluginJar.renameTo(new File(devPaths.getPluginStorageDir(), pluginJar.getName())));

        String output = setupAndSubmitPersonalBuild();
        assertThat(output, containsString(PROMPT_SYNC_REQUIRED));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString(OUTPUT_RESTARTING));
        assertThat(output, containsString(OUTPUT_PATCH_ACCEPTED));

        rpcClient.RemoteApi.waitForBuildToComplete(extractBuildNumber(output));
    }
    
    private String setupAndSubmitPersonalBuild() throws Exception
    {
        String projectName = getName() + RandomUtils.insecureRandomString(10);
        rpcClient.RemoteApi.insertSimpleProject(projectName);
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
