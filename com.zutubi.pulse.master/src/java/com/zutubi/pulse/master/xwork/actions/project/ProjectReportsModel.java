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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.xwork.actions.GraphModel;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines JSON structure for the project reports page.
 */
public class ProjectReportsModel
{
    private String group;
    private List<String> groupNames;
    private int timeFrame;
    private String timeUnit;
    private int buildCount;
    private List<GraphModel> reports = new LinkedList<GraphModel>();

    public ProjectReportsModel(String group, List<String> groupNames, int timeFrame, String timeUnit, int buildCount)
    {
        this.group = group;
        this.groupNames = groupNames;
        this.timeFrame = timeFrame;
        this.timeUnit = timeUnit;
        this.buildCount = buildCount;
    }

    public String getGroup()
    {
        return group;
    }

    @JSON
    public List<String> getGroupNames()
    {
        return groupNames;
    }

    public int getTimeFrame()
    {
        return timeFrame;
    }

    public String getTimeUnit()
    {
        return timeUnit;
    }

    public int getBuildCount()
    {
        return buildCount;
    }

    @JSON
    public List<GraphModel> getReports()
    {
        return reports;
    }

    public void addReport(GraphModel report)
    {
        reports.add(report);
    }

}
