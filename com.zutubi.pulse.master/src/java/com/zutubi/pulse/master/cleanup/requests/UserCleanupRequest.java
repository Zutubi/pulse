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

package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.BuildCleanupOptions;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;

import java.util.List;

/**
 * A request to clean up the personal builds for a specific user.  The
 * number of builds retained is defined by the users preferences.
 */
public class UserCleanupRequest extends EntityCleanupRequest
{
    private BuildResultDao buildResultDao;
    private BuildManager buildManager;

    private User user;

    public UserCleanupRequest(User user)
    {
        super(user);
        this.user = user;
    }

    public void run()
    {
        List<BuildResult> builds = buildResultDao.getOldestCompletedBuilds(user, user.getPreferences().getMyBuildsCount());
        for(BuildResult build: builds)
        {
            buildManager.cleanup(build, new BuildCleanupOptions(true));
        }
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
