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

package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 */
public class BrowseProjectActionResolver extends StaticMapActionResolver
{
    public BrowseProjectActionResolver(String project)
    {
        super("projectHome");

        addMapping("home", new ParameterisedActionResolver("projectHome"));
        addMapping("reports", new ProjectReportsActionResolver());
        addMapping("history", new PagedActionResolver("projectHistory"));
        addMapping("dependencies", new ParameterisedActionResolver("projectDependencies"));
        addMapping("log", new ProjectLogActionResolver());
        addMapping("builds", new ProjectBuildsActionResolver());
        addMapping("changes", new ChangelistsActionResolver());
        addMapping("actions", new ProjectActionsActionResolver());

        addParameter("projectName", project);
    }
}
