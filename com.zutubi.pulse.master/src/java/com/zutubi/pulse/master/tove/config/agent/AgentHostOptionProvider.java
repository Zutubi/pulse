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

package com.zutubi.pulse.master.tove.config.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;
import com.zutubi.util.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Iterables.*;

/**
 * Lists currently-used agent hosts as an extra hint to the user that they can
 * run more than one agent on a single host.
 */
public class AgentHostOptionProvider extends ListOptionProvider
{
    private AgentManager agentManager;

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return "";
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        Set<String> uniqueHosts = new TreeSet<String>(new Sort.StringComparator());
        addAll(uniqueHosts, filter(transform(agentManager.getAllAgents(), new Function<Agent, String>()
        {
            public String apply(Agent agent)
            {
                return agent.getConfig().getHost();
            }
        }), Predicates.notNull()));

        return new LinkedList<String>(uniqueHosts);
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
