package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.acegisecurity.acl.basic.AclObjectIdentityAware;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class Project extends Entity implements AclObjectIdentity, AclObjectIdentityAware
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
    private Long lastPollTime;
    private boolean forceClean = false;

    // Not sure where the ACLs belong, in the database or in the configuration
    private List<ProjectAclEntry> aclEntries;

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

    public boolean isForceClean()
    {
        return forceClean;
    }

    public void setForceClean(boolean forceClean)
    {
        this.forceClean = forceClean;
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

    public synchronized void buildCommenced()
    {
        state = State.BUILDING;
    }

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

    public AclObjectIdentity getAclObjectIdentity()
    {
        return this;
    }

    public List<ProjectAclEntry> getAclEntries()
    {
        if (aclEntries == null)
        {
            aclEntries = new LinkedList<ProjectAclEntry>();
        }
        return aclEntries;
    }

    private void setAclEntries(List<ProjectAclEntry> aclEntries)
    {
        this.aclEntries = aclEntries;
    }

    public boolean hasAdmin(String login)
    {
        for(ProjectAclEntry acl: getAclEntries())
        {
            if(acl.getRecipient().equals(login))
            {
                return true;
            }
        }

        return false;
    }

    public void addAdmin(String recipient)
    {
        if(!hasAdmin(recipient))
        {
            getAclEntries().add(new ProjectAclEntry(recipient, this, ProjectAclEntry.WRITE));
        }
    }

    public void removeAdmin(String login)
    {
        ProjectAclEntry remove = null;
        for(ProjectAclEntry entry: getAclEntries())
        {
            if(entry.getRecipient().equals(login))
            {
                remove = entry;
                break;
            }
        }

        if(remove != null)
        {
            getAclEntries().remove(remove);
        }
    }

}
