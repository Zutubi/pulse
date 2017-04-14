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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.ui.links.ConfigurationLink;
import com.zutubi.util.WebUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Links from the project configuration page to related project pages.
 */
public class ProjectConfigurationLinks
{
    public List<ConfigurationLink> getLinks(ProjectConfiguration projectConfiguration)
    {
        if (projectConfiguration.isConcrete())
        {
            Urls urls = Urls.getBaselessInstance();
            String name = WebUtils.uriComponentEncode(projectConfiguration.getName());
            return Arrays.asList(
                    new ConfigurationLink("home", urls.projectHome(name)),
                    new ConfigurationLink("reports", urls.projectReports(name)),
                    new ConfigurationLink("history", urls.projectHistory(name)),
                    new ConfigurationLink("dependencies", urls.projectDependencies(name)),
                    new ConfigurationLink("log", urls.projectLog(name))
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
