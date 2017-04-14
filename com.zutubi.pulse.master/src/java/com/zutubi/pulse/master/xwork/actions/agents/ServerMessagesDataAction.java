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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.HostManager;
import com.zutubi.pulse.master.agent.HostService;
import com.zutubi.pulse.master.xwork.actions.project.PagerModel;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.List;

/**
 * An action to yield JSON data for the server and agent messages tabs.
 */
public class ServerMessagesDataAction extends AgentActionBase
{
    private static final Logger LOG = Logger.getLogger(ServerMessagesDataAction.class);

    private static final int ENTRIES_PER_PAGE = 10;

    private int startPage;

    private ServerMessagesModel model;

    private ServerMessagesHandler serverMessagesHandler;
    private HostManager hostManager;

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public ServerMessagesModel getModel()
    {
        return model;
    }

    public String execute()
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        Agent agent = getAgent();
        List<CustomLogRecord> records;
        if (agent == null)
        {
            records = serverMessagesHandler.takeSnapshot();
        }
        else
        {
            if (agent.isOnline())
            {
                HostService hostService = hostManager.getServiceForHost(agent.getHost());
                try
                {
                    records = hostService.getRecentMessages();
                }
                catch(HessianRuntimeException e)
                {
                    LOG.warning(e);
                    throw new RuntimeException("Unable to contact agent: " + e.getMessage());
                }
            }
            else
            {
                throw new RuntimeException("Agent is not online.");
            }
        }

        int totalItems = records.size();
        int pageCount = (totalItems + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
        if (startPage >= pageCount)
        {
            startPage = pageCount - 1;
        }

        if (startPage < 0)
        {
            startPage = 0;
        }

        int startOffset = startPage * ENTRIES_PER_PAGE;
        int endOffset = getEndOffset(startOffset, totalItems);
        Collections.reverse(records);
        records = records.subList(startOffset, endOffset);

        model = new ServerMessagesModel();
        for (CustomLogRecord record: records)
        {
            model.addEntry(new LogEntryModel(record));
        }

        model.setPager(new PagerModel(totalItems, ENTRIES_PER_PAGE, startPage));
        return SUCCESS;
    }

    public int getEndOffset(int startOffset, int totalItems)
    {
        int offset = startOffset + ENTRIES_PER_PAGE;
        if (offset > totalItems)
        {
            offset = totalItems;
        }

        return offset;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }
}
