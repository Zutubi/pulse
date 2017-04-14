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

package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.User;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Controls access to cancellation of build requests.
 */
public class BuildRequestEventAuthorityProvider implements AuthorityProvider<BuildRequestEvent>
{
    private ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, BuildRequestEvent resource)
    {
        if(resource.isPersonal())
        {
            User user = (User) resource.getOwner();
            Set<String> result = new HashSet<String>();
            result.add(user.getConfig().getDefaultAuthority());
            return result;
        }
        else
        {
            return projectConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getProjectConfig());
        }
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(BuildRequestEvent.class, this);
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
