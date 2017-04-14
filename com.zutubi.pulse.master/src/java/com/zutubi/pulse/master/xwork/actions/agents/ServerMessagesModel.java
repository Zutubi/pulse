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

import com.zutubi.pulse.master.xwork.actions.project.PagerModel;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON data model for server and agent messages tabs.
 */
public class ServerMessagesModel
{
    private List<LogEntryModel> entries = new LinkedList<LogEntryModel>();
    private PagerModel pager;

    public List<LogEntryModel> getEntries()
    {
        return entries;
    }

    public void addEntry(LogEntryModel entry)
    {
        entries.add(entry);
    }

    public PagerModel getPager()
    {
        return pager;
    }

    public void setPager(PagerModel pager)
    {
        this.pager = pager;
    }
}
