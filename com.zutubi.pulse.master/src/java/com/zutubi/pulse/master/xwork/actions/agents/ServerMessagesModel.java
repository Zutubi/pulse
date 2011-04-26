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
