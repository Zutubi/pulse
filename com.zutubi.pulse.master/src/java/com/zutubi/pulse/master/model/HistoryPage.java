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

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.agent.Agent;

import java.util.List;

/**
 */
public class HistoryPage
{
    private Project[] projects;
    private Agent agent;
    private int totalBuilds;
    private int first;
    private int max;
    List<BuildResult> results;

    public HistoryPage(Project[] projects, Agent agent, int first, int max)
    {
        this.projects = projects;
        this.agent = agent;
        this.first = first;
        this.max = max;
    }

    public Project[] getProjects()
    {
        return projects;
    }

    public Agent getAgent()
    {
        return agent;
    }

    public int getTotalBuilds()
    {
        return totalBuilds;
    }

    public void setTotalBuilds(int totalBuilds)
    {
        this.totalBuilds = totalBuilds;
    }

    public int getFirst()
    {
        return first;
    }

    public void setFirst(int first)
    {
        this.first = first;
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public List<BuildResult> getResults()
    {
        return results;
    }

    public void setResults(List<BuildResult> results)
    {
        this.results = results;
    }
}
