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

import com.zutubi.pulse.master.tove.model.ActionLink;
import flexjson.JSON;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for JSON data used to render the project home page.
 */
public class ProjectHomeModel
{
    private ProjectResponsibilityModel responsibility;
    private StatusModel status;
    private List<BuildModel> activity = new ArrayList<BuildModel>();
    private List<BuildModel> recent = new ArrayList<BuildModel>();
    private List<ChangelistModel> changes = new ArrayList<ChangelistModel>();
    private String description;
    private List<ActionLink> actions = new ArrayList<ActionLink>();
    private List<ActionLink> links = new ArrayList<ActionLink>();
    private String url;

    public ProjectHomeModel(StatusModel status)
    {
        this.status = status;
    }

    public ProjectResponsibilityModel getResponsibility()
    {
        return responsibility;
    }

    public void setResponsibility(ProjectResponsibilityModel responsibility)
    {
        this.responsibility = responsibility;
    }

    public StatusModel getStatus()
    {
        return status;
    }

    @JSON
    public List<BuildModel> getActivity()
    {
        return activity;
    }
    
    @JSON
    public List<BuildModel> getRecent()
    {
        return recent;
    }
    
    @JSON
    public List<ChangelistModel> getChanges()
    {
        return changes;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @JSON
    public List<ActionLink> getActions()
    {
        return actions;
    }
    
    public void addAction(ActionLink action)
    {
        actions.add(action);
    }

    @JSON
    public List<ActionLink> getLinks()
    {
        return links;
    }
    
    public void addLink(ActionLink link)
    {
        links.add(link);
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public static class StatusModel
    {
        private String name;
        private String health;
        private StateModel state;
        private int successRate;
        private StatisticsModel statistics;
        private List<BuildStageModel> brokenStages;

        public StatusModel(String name, String health, StateModel state, StatisticsModel statistics, List<BuildStageModel> brokenStages)
        {
            this.name = name;
            this.health = health;
            this.state = state;
            // errors / terminated states are excluded from the success rate statistic
            this.successRate = (int) Math.round((statistics.getOk()) * 100.0 / (statistics.getFailed() + statistics.getOk()));
            this.statistics = statistics;
            this.brokenStages = brokenStages;
        }

        public String getName()
        {
            return name;
        }

        public String getHealth()
        {
            return health;
        }

        public StateModel getState()
        {
            return state;
        }

        public int getSuccessRate()
        {
            return successRate;
        }

        public StatisticsModel getStatistics()
        {
            return statistics;
        }

        @JSON
        public List<BuildStageModel> getBrokenStages()
        {
            return brokenStages;
        }
    }

    public static class StateModel
    {
        private String pretty;
        private String keyTransition;

        public StateModel(String pretty, String keyTransition)
        {
            this.pretty = pretty;
            this.keyTransition = keyTransition;
        }

        public String getPretty()
        {
            return pretty;
        }

        public String getKeyTransition()
        {
            return keyTransition;
        }
    }

    public static class StatisticsModel
    {
        private int total;
        private int ok;
        private int failed;

        public StatisticsModel(int total, int ok, int failed)
        {
            this.total = total;
            this.ok = ok;
            this.failed = failed;
        }

        public int getTotal()
        {
            return total;
        }

        public int getOk()
        {
            return ok;
        }

        public int getFailed()
        {
            return failed;
        }
    }

}
