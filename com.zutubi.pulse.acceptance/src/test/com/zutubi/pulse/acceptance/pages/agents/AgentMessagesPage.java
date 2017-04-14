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

package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.server.ServerMessagesPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The agent messages page shows recent agent log messages.
 */
public class AgentMessagesPage extends ServerMessagesPage
{
    private String agent;

    public AgentMessagesPage(SeleniumBrowser browser, Urls urls, String agent, int page)
    {
        super(browser, urls, page);
        this.agent = agent;
    }

    public String getUrl()
    {
        if (getPage() == 1)
        {
            return urls.agentMessages(agent);
        }
        else
        {
            return urls.agentMessages(agent) + Integer.toString(getPage() - 1) + "/";
        }
    }

    @Override
    public ServerMessagesPage createNextPage()
    {
        return new AgentMessagesPage(browser, urls, agent, getPage() + 1);
    }
}
