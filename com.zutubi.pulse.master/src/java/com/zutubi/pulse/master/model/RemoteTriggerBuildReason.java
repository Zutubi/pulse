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
 * The build was triggered via the remote API with no additional reason specified.
 */
public class RemoteTriggerBuildReason extends AbstractBuildReason
{
    private String username;

    public RemoteTriggerBuildReason()
    {
    }

    public RemoteTriggerBuildReason(String username)
    {
        this.username = username;
    }

    public boolean isUser()
    {
        return true;
    }

    public String getSummary()
    {
        return "trigger via remote api by " + username;
    }

    public String getUsername()
    {
        return username;
    }

    /**
     * Used by hibernate.
     *
     * @param username property value.
     */
    private void setUsername(String username)
    {
        this.username = username;
    }
}
