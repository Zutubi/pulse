package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Corresponds to the Zutubi.pulse.project.BuildSummaryTable component.
 */
public class BuildSummaryTable extends SummaryTable
{
    public BuildSummaryTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Returns information about the builds in the table.
     * 
     * @return information about the builds in this table
     */
    public List<BuildInfo> getBuilds()
    {
        List<BuildInfo> result = new LinkedList<BuildInfo>();
        int count = getRowCount();
        for (int i = 0; i < count; i++)
        {
            result.add(new BuildInfo(getRow(i)));
        }

        return result;
    }
    
}
