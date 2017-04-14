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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

/**
 * Abstract base for subscriptions.
 */
@SymbolicName("zutubi.subscriptionConfig")
@Table(columns = {"name"})
@Classification(collection = "favourites")
public abstract class SubscriptionConfiguration extends AbstractNamedConfiguration
{
    @Required
    @Reference
    private ContactConfiguration contact;
    @Dropdown(optionProvider = "SubscriptionTemplateOptionProvider")
    private String template;
    @ControllingCheckbox(checkedFields = "logLineLimit")
    private boolean attachLogs;
    @Numeric(min = 0, max = 250)
    private int logLineLimit = 50;

    public ContactConfiguration getContact()
    {
        return contact;
    }

    public void setContact(ContactConfiguration contact)
    {
        this.contact = contact;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public boolean isAttachLogs()
    {
        return attachLogs;
    }

    public void setAttachLogs(boolean attachLogs)
    {
        this.attachLogs = attachLogs;
    }

    public int getLogLineLimit()
    {
        return logLineLimit;
    }

    public void setLogLineLimit(int logLineLimit)
    {
        this.logLineLimit = logLineLimit;
    }

    public abstract boolean conditionSatisfied(NotifyConditionContext context);
}
