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
import com.zutubi.util.Constants;
import com.zutubi.util.time.TimeStamps;

public class UserConfigurationFormatter
{
    private LastAccessManager lastAccessManager;

    public String getActive(UserConfiguration userConfig)
    {
        long now = System.currentTimeMillis();

        String accessElapsed;
        long time = lastAccessManager.getLastAccessTime(userConfig.getUserId());
        if (time != LastAccessManager.ACCESS_NEVER)
        {
            long elapsed = now - time;
            if (elapsed < Constants.SECOND)
            {
                accessElapsed = "< 1 second ago";
            }
            else
            {
                int maxUnits = (elapsed < Constants.MINUTE) ? 1 : 2;
                accessElapsed = TimeStamps.getPrettyElapsed(elapsed, maxUnits) + " ago";
            }
        }
        else
        {
            accessElapsed = "never logged in";
        }

        String prefix = lastAccessManager.isActive(userConfig.getUserId(), now) ? "yes" : "no";
        return prefix + " (" + accessElapsed + ")";
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}
