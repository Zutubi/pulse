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

package com.zutubi.pulse.acceptance.components.pulse.project;

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
        long count = getRowCount();
        for (int i = 0; i < count; i++)
        {
            result.add(new BuildInfo(getRow(i)));
        }

        return result;
    }
    
}
