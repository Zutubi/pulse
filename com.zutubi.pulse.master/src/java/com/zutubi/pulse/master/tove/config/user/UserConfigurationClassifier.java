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

import com.zutubi.pulse.master.security.LastAccessManager;
import com.zutubi.tove.config.api.Classifier;

/**
 * Classifies users.
 */
public class UserConfigurationClassifier implements Classifier<UserConfiguration>
{
    private static final String CLASS_ACTIVE = "active-user";
    private static final String CLASS_NORMAL = "user";

    private LastAccessManager lastAccessManager;

    public String classify(UserConfiguration userConfiguration)
    {
        if (userConfiguration != null && lastAccessManager.isActive(userConfiguration.getUserId(), System.currentTimeMillis()))
        {
            return CLASS_ACTIVE;
        }
        else
        {
            return CLASS_NORMAL;
        }
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}
