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

package com.zutubi.pulse.master.model;

/**
 * Indicates that the build occurred because a user manually fired a configured trigger.
 */
public class NamedManualTriggerBuildReason extends AbstractBuildReason
{
    private String triggerName;
    private String username;

    public NamedManualTriggerBuildReason()
    {
    }

    public NamedManualTriggerBuildReason(String triggerName, String username)
    {
        this.triggerName = triggerName;
        this.username = username;
    }

    public boolean isUser()
    {
        return true;
    }

    public String getSummary()
    {
        return "'" + triggerName + "' fired by " + (username != null ? username : "anonymous");
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    /**
     * @return the user and trigger names safely combined into one field, so they can be stored in
     *         one database column (like all other build reasons are)
     */
    public String getNames()
    {
        if (username == null)
        {
            return triggerName;
        }
        else
        {
            return triggerName + "/" + username;
        }
    }

    private void setNames(String names)
    {
        int index = names.indexOf('/');
        if (index < 0)
        {
            triggerName = names;
        }
        else
        {
            triggerName = names.substring(0, index);
            if (index < names.length() - 1)
            {
                username = names.substring(index + 1, names.length());
            }
            else
            {
                username = "";
            }
        }
    }
}
