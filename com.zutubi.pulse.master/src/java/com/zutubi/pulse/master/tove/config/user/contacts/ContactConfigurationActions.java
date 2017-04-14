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

package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.notifications.ResultNotifier;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Action processing for {@link com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration}'s.
 */
public class ContactConfigurationActions
{
    public static final String ACTION_CLEAR_ERROR = "clearError";
    public static final String ACTION_MARK_PRIMARY = "markPrimary";

    private ResultNotifier resultNotifier;
    private ConfigurationProvider configurationProvider;

    public List<String> getActions(ContactConfiguration contactConfiguration)
    {
        List<String> actions = new LinkedList<String>();
        if (resultNotifier.hasError(contactConfiguration))
        {
            actions.add(ACTION_CLEAR_ERROR);
        }

        if (!contactConfiguration.isPrimary())
        {
            actions.add(ACTION_MARK_PRIMARY);
        }

        return actions;
    }

    public void doClearError(ContactConfiguration contactConfiguration)
    {
        resultNotifier.clearError(contactConfiguration);
    }

    public void doMarkPrimary(ContactConfiguration contactConfiguration)
    {
        if (!contactConfiguration.isPrimary())
        {
            UserPreferencesConfiguration preferences = configurationProvider.getAncestorOfType(contactConfiguration.getConfigurationPath(), UserPreferencesConfiguration.class);
            for (ContactConfiguration contact: preferences.getContacts().values())
            {
                if (contact.isPrimary())
                {
                    contact = configurationProvider.deepClone(contact);
                    contact.setPrimary(false);
                    configurationProvider.save(contact);
                }
            }

            contactConfiguration = configurationProvider.deepClone(contactConfiguration);
            contactConfiguration.setPrimary(true);
            configurationProvider.save(contactConfiguration);
        }
    }

    public void setResultNotifier(ResultNotifier resultNotifier)
    {
        this.resultNotifier = resultNotifier;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
