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

import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.xwork.actions.project.CommentSummaryModel;
import com.zutubi.pulse.servercore.services.HostStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a row in the agents table.
 */
public class AgentRowModel
{
    private long id;
    private String name;
    private String location;
    private String status;
    private long recipeId;
    private ExecutingStageModel executingStage;
    private CommentSummaryModel comments;
    private List<ActionLink> actions = new LinkedList<ActionLink>();

    public AgentRowModel(long id, String name, String location, String status, long recipeId, CommentSummaryModel comments)
    {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
        this.recipeId = recipeId;
        this.comments = comments;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getLocation()
    {
        return location;
    }

    public String getStatus()
    {
        return status;
    }

    public boolean isExecuting()
    {
        return recipeId != HostStatus.NO_RECIPE;
    }

    public ExecutingStageModel getExecutingStage()
    {
        return executingStage;
    }

    public void setExecutingStage(ExecutingStageModel executingStage)
    {
        this.executingStage = executingStage;
    }

    public CommentSummaryModel getComments()
    {
        return comments;
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public void addAction(ActionLink action)
    {
        actions.add(action);
    }
}
