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

package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 */
public class AgentActionResolver extends StaticMapActionResolver
{
    public AgentActionResolver(String agent)
    {
        super("agentStatus");
        addParameter("agentName", agent);
        addMapping("actions", new AgentActionsActionResolver());
        addMapping("status", new ParameterisedActionResolver("agentStatus"));
        addMapping("statistics", new ParameterisedActionResolver("agentStatistics"));
        addMapping("history", new PagedActionResolver("agentHistory"));
        addMapping("messages", new PagedActionResolver("serverMessages"));
        addMapping("info", new ParameterisedActionResolver("serverInfo"));
    }
}
