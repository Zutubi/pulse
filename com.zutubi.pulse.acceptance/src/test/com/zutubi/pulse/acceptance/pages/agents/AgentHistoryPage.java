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
import com.zutubi.pulse.acceptance.pages.AbstractHistoryPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * Shows completed builds for that involved an agent.
 */
public class AgentHistoryPage extends AbstractHistoryPage
{
    private String agentName;

    public AgentHistoryPage(SeleniumBrowser browser, Urls urls, String agentName)
    {
        super(browser, urls, "agent-history-" + agentName, "agent-history");
        this.agentName = agentName;
    }

    @Override
    public String getUrl()
    {
        return urls.agentHistory(WebUtils.uriComponentEncode(agentName));
    }
}
