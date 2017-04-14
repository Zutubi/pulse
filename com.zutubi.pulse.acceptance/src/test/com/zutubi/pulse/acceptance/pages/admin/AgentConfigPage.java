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

package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The page shown when looking at an agent in the configuration view.
 */
public class AgentConfigPage extends CompositePage
{
    private String agent;
    private boolean template;

    public AgentConfigPage(SeleniumBrowser browser, Urls urls, String agent, boolean template)
    {
        super(browser, urls, getPath(agent));
        this.agent = agent;
        this.template = template;
    }

    private static String getPath(String agent)
    {
        return "agents/" + agent;
    }

    public String getPath()
    {
        return getPath(agent);
    }

    public String getUrl()
    {
        return urls.adminAgent(WebUtils.uriComponentEncode(agent));
    }

    public AgentHierarchyPage clickHierarchy()
    {
        browser.click(By.xpath(getHierarchyXPath()));
        return browser.createPage(AgentHierarchyPage.class, agent, template);
    }
}
