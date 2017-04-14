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

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.notifications.ResultNotifier;
import com.zutubi.tove.config.ConfigurationList;
import com.zutubi.tove.ui.format.MessagesAware;

import java.util.List;

/**
 * Shows a contact point error if one exists.
 */
public class ContactConfigurationStateDisplay implements MessagesAware
{
    public static final String FIELD_LAST_ERROR = "lastError";
    public static final String FIELD_PRIMARY = "primary";

    private ResultNotifier resultNotifier;
    private Messages messages;

    public List<String> getFields(ContactConfiguration contactConfiguration)
    {
        List<String> fields = new ConfigurationList<String>();
        if (resultNotifier.hasError(contactConfiguration))
        {
            fields.add(FIELD_LAST_ERROR);
        }

        if (contactConfiguration.isPrimary())
        {
            fields.add(FIELD_PRIMARY);
        }

        return fields;
    }

    public String formatLastError(ContactConfiguration contactConfiguration)
    {
        return resultNotifier.getError(contactConfiguration);
    }

    public String formatPrimary()
    {
        return messages.format("primary.blurb");
    }

    public void setResultNotifier(ResultNotifier resultNotifier)
    {
        this.resultNotifier = resultNotifier;
    }

    public void setMessages(Messages messages)
    {
        this.messages = messages;
    }
}
