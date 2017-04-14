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

import com.zutubi.pulse.acceptance.forms.setup.*;
import com.zutubi.pulse.acceptance.pages.PulseToolbar;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;

/**
 * A setup test that covers the systems setup procedure.
 * <p/>
 * This setup test is a little awkward since we can only run it once. Once done, the
 * server is setup and will not take kindly to us trying to set it up again. So, rather than
 * having multiple test methods, there is one testSetupProcess method that is breaks up the setup
 * process and handles all of the validation testing as it goes.
 */
public class SetupAcceptanceTest extends AcceptanceTestBase
{
    public void testSetupProcess() throws Exception
    {
        getBrowser().open(urls.base() + "setup/setupData!input.action");

        checkSetPulseData();
        checkPostPulseData();

        setAdminPreferences();
    }

    private void checkSetPulseData()
    {
        SetPulseDataForm form = getBrowser().createForm(SetPulseDataForm.class);
        form.waitFor();
        assertTrue(form.isBrowseDataLinkPresent());
        assertTrue(form.isFieldNotEmpty("zfid.data"));

        form.nextFormElements("");
        form.waitFor();
        getBrowser().waitForTextPresent("pulse data directory requires a value");

        form.nextFormElements("data");
    }

    protected void checkPostPulseData()
    {
        checkSetupDatabase();
        checkCreateAdmin();
        checkServerSettings();

        getBrowser().waitForTextPresent(":: welcome ::");

        PulseToolbar toolbar = new PulseToolbar(getBrowser());
        toolbar.waitFor();

        getBrowser().waitForTextPresent("A. D. Ministrator");
        assertTrue(getBrowser().isElementIdPresent("logout"));
    }

    private void checkSetupDatabase()
    {
        SetupDatabaseTypeForm form = getBrowser().createForm(SetupDatabaseTypeForm.class);
        form.waitFor();
        assertFalse("Detail fields should be disabled for embedded database", form.isEditable("host"));
        form.nextFormElements("EMBEDDED", null, null, null, null, null, null);
    }

    private void checkCreateAdmin()
    {
        CreateAdminForm createAdminForm = getBrowser().createForm(CreateAdminForm.class);
        createAdminForm.waitFor();
        createAdminForm.nextFormElements(
                ADMIN_CREDENTIALS.getUserName(),
                "A. D. Ministrator",
                "admin@example.com",
                ADMIN_CREDENTIALS.getPassword(),
                ADMIN_CREDENTIALS.getPassword()
        );
    }

    private void checkServerSettings()
    {
        ServerSettingsForm settingsForm = getBrowser().createForm(ServerSettingsForm.class);
        settingsForm.waitFor();
        settingsForm.finishFormElements("http://localhost:8080", "some.smtp.host.com", "true", "Setup <from@localhost.com>", "username", "password", "prefix", "true", "123");
    }

    private void setAdminPreferences() throws Exception
    {
        rpcClient.loginAsAdmin();
        try
        {
            String preferencesPath = PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, ADMIN_CREDENTIALS.getUserName(), "preferences");
            Hashtable<String, Object> preferences = rpcClient.RemoteApi.getConfig(preferencesPath);
            preferences.put("refreshInterval", 600);
            rpcClient.RemoteApi.saveConfig(preferencesPath, preferences, false);
        }
        finally
        {
            rpcClient.logout();
        }
    }
}
