package com.zutubi.pulse.model;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.acegisecurity.acl.basic.AclObjectIdentityAware;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class Project extends Entity implements AclObjectIdentity, AclObjectIdentityAware, NamedEntity
{
    public enum State
    {
        /**
         * There is a build running for this project.
         */
        BUILDING,
        /**
         * No builds running for the project at the moment.
         */
        IDLE,
        /**
         * Currently paused: triggers will be ignored while in this state.
         */
        PAUSED,
        /**
         * Project is building, but will be paused when the current build
         * is completed.
         */
        PAUSING
    }

    private State state = State.IDLE;
    private long nextBuildNumber = 1;
    private int buildCount = 0;
    private int successCount = 0;
    private Long lastPollTime;
    private ProjectConfiguration config;
    private List<AgentState> forceCleanAgents = new LinkedList<AgentState>();

    public Project()
    {
    }

    public Long getLastPollTime()
    {
        return lastPollTime;
    }

    public void setLastPollTime(Long lastPollTime)
    {
        this.lastPollTime = lastPollTime;
    }

    public State getState()
    {
        return state;
    }

    /**
     * used by hibernate.
     */
    private String getStateName()
    {
        return state.toString();
    }

    /**
     * used by hibernate.
     */
    private void setStateName(String stateName)
    {
        state = State.valueOf(stateName);
    }

    public synchronized boolean isPaused()
    {
        return state == State.PAUSED || state == State.PAUSING;
    }

    /**
     * Update the state of the project to indicate that the build has commenced.
     */
    public synchronized void buildCommenced()
    {
        state = State.BUILDING;
    }

    /**
     * Update the state of the project to indicate that the build has been completed.
     */
    public synchronized void buildCompleted()
    {
        if (state == State.PAUSING)
        {
            state = State.PAUSED;
        }
        else
        {
            state = State.IDLE;
        }
    }

    /**
     * Update the state of the project to indicate that this project is paused.
     */
    public synchronized void pause()
    {
        switch (state)
        {
            case BUILDING:
                state = State.PAUSING;
                break;
            case IDLE:
                state = State.PAUSED;
                break;
        }
    }

    /**
     * If this project is paused, then update the state of this project to indicate that
     * building can be resumed.  
     */
    public synchronized void resume()
    {
        switch (state)
        {
            case PAUSED:
                state = State.IDLE;
                break;
            case PAUSING:
                state = State.BUILDING;
                break;
        }
    }

    public long getNextBuildNumber()
    {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(long nextBuildNumber)
    {
        this.nextBuildNumber = nextBuildNumber;
    }

    public int getBuildCount()
    {
        return buildCount;
    }

    public void setBuildCount(int buildCount)
    {
        this.buildCount = buildCount;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }

    public AclObjectIdentity getAclObjectIdentity()
    {
        return this;
    }

    public ProjectConfiguration getConfig()
    {
        return config;
    }

    public void setConfig(ProjectConfiguration config)
    {
        this.config = config;
    }

    public String getName()
    {
        return config == null ? null : config.getName();
    }

    public String getUrl()
    {
        return config == null ? null : config.getUrl();
    }

    public String getDescription()
    {
        return config == null ? null : config.getDescription();
    }

    public List<AgentState> getForceCleanAgents()
    {
        return forceCleanAgents;
    }

    public boolean isForceCleanForAgent(final long agentStateId)
    {
        return CollectionUtils.find(forceCleanAgents, new Predicate<AgentState>()
        {
            public boolean satisfied(AgentState agentState)
            {
                return agentState.getId() == agentStateId;
            }
        }) != null;
    }

    public void setForceCleanAgents(List<AgentState> forceCleanAgents)
    {
        this.forceCleanAgents = forceCleanAgents;
    }

    public boolean setForceCleanForAgent(AgentState agentState)
    {
        if(!isForceCleanForAgent(agentState.getId()))
        {
            forceCleanAgents.add(agentState);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean clearForceCleanForAgent(final long agentStateId)
    {
        int sizeBefore = forceCleanAgents.size();
        forceCleanAgents = CollectionUtils.filter(forceCleanAgents, new Predicate<AgentState>()
        {
            public boolean satisfied(AgentState agentState)
            {
                return agentState.getId() != agentStateId;
            }
        });

        return forceCleanAgents.size() != sizeBefore;
    }
}
