package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.model.AgentSynchronisationMessage;

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
    private ExecutingStageModel executingStage;
    private List<SynchronisationMessageModel> synchronisationMessages = new LinkedList<SynchronisationMessageModel>();

    public AgentStatusModel(String name, String location)
    {
        info = new AgentModel(name, location);
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

    public ExecutingStageModel getExecutingStage()
    {
        return executingStage;
    }

    public void setExecutingStage(ExecutingStageModel executingStage)
    {
        this.executingStage = executingStage;
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

        public AgentModel(String name, String location)
        {
            this.name = name;
            this.location = location;
        }

        public String getName()
        {
            return name;
        }

        public String getLocation()
        {
            return location;
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
