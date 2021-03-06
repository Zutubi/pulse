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

package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Pager;
import com.zutubi.pulse.acceptance.components.pulse.server.LogMessagesTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server messages page shows recent server log messages.
 */
public class ServerMessagesPage extends SeleniumPage
{
    private LogMessagesTable entries;
    private Pager pager;
    private int page = 1;

    public ServerMessagesPage(SeleniumBrowser browser, Urls urls, int page)
    {
        super(browser, urls, "server-messages-" + Integer.toString(page - 1));
        this.page = page;
        entries = new LogMessagesTable(browser, getId() + "-entries");
        pager = new Pager(browser, getId() + "-pager");
    }

    public String getUrl()
    {
        if (page == 1)
        {
            return urls.serverMessages();
        }
        else
        {
            return urls.serverMessages() + Integer.toString(page - 1) + "/";
        }
    }

    public int getPage()
    {
        return page;
    }

    public LogMessagesTable getEntries()
    {
        return entries;
    }

    public Pager getPager()
    {
        return pager;
    }

    public ServerMessagesPage createNextPage()
    {
        return new ServerMessagesPage(browser, urls, page + 1);
    }
}
