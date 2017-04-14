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

package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.xwork.actions.agents.ServerMessagesActionSupport;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.tove.security.AccessManager;

import java.util.Collections;
import java.util.List;

/**
 * Looks up recent errors to show on the internal error page.
 */
public class InternalErrorAction extends ServerMessagesActionSupport
{
    private List<CustomLogRecord> records;

    public List<CustomLogRecord> getRecords()
    {
        return records;
    }

    public String execute() throws Exception
    {
        if (accessManager.hasPermission(AccessManager.ACTION_ADMINISTER, null))
        {
            records = serverMessagesHandler.takeSnapshot();
            Collections.reverse(records);
            if (records.size() > 4)
            {
                records = records.subList(0, 4);
            }
        }

        return SUCCESS;
    }
}
