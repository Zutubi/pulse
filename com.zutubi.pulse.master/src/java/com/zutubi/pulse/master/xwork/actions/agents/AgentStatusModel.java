package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.xwork.actions.CommentModel;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * JSON model for the agent status tab.
 */
public class AgentStatusModel
{
    private AgentModel info;
    private Map<String, String> status = new LinkedHashMap<String, String>();
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private ExecutingStageModel executingStage;
    private List<CommentModel> comments = new LinkedList<CommentModel>();
    private List<SynchronisationMessageModel> synchronisationMessages = new LinkedList<SynchronisationMessageModel>();

    public AgentStatusModel(String name, String location, boolean canViewMessages)
    {
        info = new AgentModel(name, location, canViewMessages);
    }

    public AgentModel getInfo()
    {
        return info;
    }

    public Map<String, String> getStatus()
    {
        return status;
    }

    public void addStatus(String name, String value)
    {
        status.put(name, value);
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public void addAction(ActionLink action)
    {
        actions.add(action);
    }
    
    public ExecutingStageModel getExecutingStage()
    {
        return executingStage;
    }

    public void setExecutingStage(ExecutingStageModel executingStage)
    {
        this.executingStage = executingStage;
    }

    public List<CommentModel> getComments()
    {
        return comments;
    }

    public void addComment(CommentModel comment)
    {
        comments.add(comment);
    }

    public List<SynchronisationMessageModel> getSynchronisationMessages()
    {
        return synchronisationMessages;
    }

    public void addSynchronisationMessages(List<AgentSynchronisationMessage> messages)
    {
        for (AgentSynchronisationMessage message: messages)
        {
            synchronisationMessages.add(new SynchronisationMessageModel(message));
        }
    }
    
    public static class AgentModel
    {
        private String name;
        private String location;
        private boolean canViewMessages;

        public AgentModel(String name, String location, boolean canViewMessages)
        {
            this.name = name;
            this.location = location;
            this.canViewMessages = canViewMessages;
        }

        public String getName()
        {
            return name;
        }

        public String getLocation()
        {
            return location;
        }

        public boolean getCanViewMessages()
        {
            return canViewMessages;
        }
    }
    
    public static class SynchronisationMessageModel
    {
        private long id;
        private String type;
        private String description;
        private String status;
        private String statusMessage;
        
        public SynchronisationMessageModel(AgentSynchronisationMessage message)
        {
            id = message.getId();
            type = message.getMessage().getType().getPrettyString();
            description = message.getDescription();
            status = message.getStatus().getPrettyString();
            statusMessage = message.getStatusMessage();
        }

        public long getId()
        {
            return id;
        }

        public String getType()
        {
            return type;
        }

        public String getDescription()
        {
            return description;
        }

        public String getStatus()
        {
            return status;
        }

        public String getStatusMessage()
        {
            return statusMessage;
        }
    }
}
