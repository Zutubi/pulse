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

package com.zutubi.pulse.dev.config;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;

import java.net.MalformedURLException;

/**
 * Utilities for setting up dev configuration for a user.
 */
public class DevConfigSetup
{
    private static final Messages I18N = Messages.getInstance(DevConfigSetup.class);

    /**
     * Prompts the user to configure their Pulse server settings (url and
     * login).
     * 
     * @param ui     interface used to prompt the user
     * @param config configuration where the settings shuld be stored
     */
    public static void setupPulseConfig(UserInterface ui, DevConfig config)
    {
        String pulseURL = getPulseURL(ui);
        String pulseUser = getPulseUser(ui);
        ui.status(I18N.format("status.storing.server", config.getUserConfigFile().getAbsolutePath()));
        config.setProperty(DevConfig.PROPERTY_PULSE_URL, pulseURL);
        config.setProperty(DevConfig.PROPERTY_PULSE_USER, pulseUser);
    }

    private static String getPulseURL(UserInterface ui)
    {
        YesNoResponse response;
        String pulseURL;

        while (true)
        {
            pulseURL = ui.inputPrompt(I18N.format("prompt.pulse.url"));

            PulseXmlRpcClient rpc;
            try
            {
                rpc = new PulseXmlRpcClient(pulseURL);
            }
            catch (MalformedURLException e)
            {
                ui.error("Invalid URL: " + e.getMessage(), e);
                continue;
            }

            try
            {
                rpc.getVersion();

                // If we got here, it worked!
                break;
            }
            catch (PulseXmlRpcException e)
            {
                ui.error("Unable to contact pulse server: " + e, e);
                response = ui.yesNoPrompt(I18N.format("prompt.url.continue"), false, false, YesNoResponse.NO);
                if (response.isAffirmative())
                {
                    break;
                }
            }
        }

        return pulseURL;
    }

    private static String getPulseUser(UserInterface ui)
    {
        String systemUser = System.getProperty("user.name");
        String pulseUser;

        String prompt = I18N.format("prompt.pulse.user");
        if (systemUser == null)
        {
            pulseUser = ui.inputPrompt(prompt);
        }
        else
        {
            pulseUser = ui.inputPrompt(prompt, systemUser);
        }
        return pulseUser;
    }
}
