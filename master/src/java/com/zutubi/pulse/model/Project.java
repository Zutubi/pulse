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
    private Long lastPollTime;
    private boolean forceClean = false;

    // Not sure where the ACLs belong, in the database or in the configuration
    private List<ProjectAclEntry> aclEntries;

    // the following fields are being transfered to the configuration system and will be
    // removed shortly.
    private String name;
    private String description;
    private String url;
    private PulseFileDetails pulseFileDetails;
    private List<PostBuildAction> postBuildActions = new LinkedList<PostBuildAction>();
    private ChangeViewer changeViewer;

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

    /**
     * @deprecated
     */
    public Project(String name, String description)
    {
        this.name = name;
        this.description = description;
        this.pulseFileDetails = new VersionedPulseFileDetails("pulse.xml");
    }

    /**
     * @deprecated
     */
    public Project(String name)
    {
        this(name, null);
    }

    /**
     * Returns a new project that is an exact replica of this project, but
     * with a different name and description.
     *
     * @param name        the name of the new project
     * @param description the description of the new project
     * @return a copy of this project with the given name
     */
    public Project copy(String name, String description)
    {
        Project copy = new Project();
        copy.name = name;
        copy.description = description;
        copy.url = url;
        copy.pulseFileDetails = pulseFileDetails.copy();

        if(changeViewer != null)
        {
            copy.changeViewer = changeViewer.copy();
        }

        copy.postBuildActions = new LinkedList<PostBuildAction>();
        for(PostBuildAction action: postBuildActions)
        {
            copy.postBuildActions.add(action.copy());
        }

        // Fix the build specification references. The copied post build actions currently reference the original
        // projects specifications. Since the specifications have themselves been copied, we need to reference the
        // new copies.
/*
        for(PostBuildAction action: copy.postBuildActions)
        {
            List<BuildSpecification> copiedSpecs = new LinkedList<BuildSpecification>();
            for (BuildSpecification originalSpec : action.getSpecifications())
            {
                // find the copied spec that matches the original.
                for (BuildSpecification copiedSpec : copy.buildSpecifications)
                {
                    if (copiedSpec.getName().equals(originalSpec.getName()))
                    {
                        copiedSpecs.add(copiedSpec);
                        break;
                    }
                }
            }
            action.setSpecifications(copiedSpecs);
        }
*/

        copy.aclEntries = new LinkedList<ProjectAclEntry>();
        for(ProjectAclEntry acl: getAclEntries())
        {
            copy.aclEntries.add(acl.copy(copy));
        }

        return copy;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public ChangeViewer getChangeViewer()
    {
        return changeViewer;
    }

    public void setChangeViewer(ChangeViewer changeViewer)
    {
        this.changeViewer = changeViewer;
    }

    public PulseFileDetails getPulseFileDetails()
    {
        return pulseFileDetails;
    }

    public void setPulseFileDetails(PulseFileDetails pulseFileDetails)
    {
        this.pulseFileDetails = pulseFileDetails;
    }

    public List<PostBuildAction> getPostBuildActions()
    {
        return postBuildActions;
    }

    private void setPostBuildActions(List<PostBuildAction> postBuildActions)
    {
        this.postBuildActions = postBuildActions;
    }

    public void addPostBuildAction(PostBuildAction action)
    {
        postBuildActions.add(action);
    }

    public void removePostBuildAction(long id)
    {
        PostBuildAction deadActionWalking = null;
        for(PostBuildAction action: postBuildActions)
        {
            if(action.getId() == id)
            {
                deadActionWalking = action;
                break;
            }
        }

        if(deadActionWalking != null)
        {
            postBuildActions.remove(deadActionWalking);
        }
    }

    public PostBuildAction getPostBuildAction(String name)
    {
        for(PostBuildAction p: postBuildActions)
        {
            if(p.getName().equals(name))
            {
                return p;
            }
        }

        return null;
    }

    public PostBuildAction getPostBuildAction(long id)
    {
        for(PostBuildAction a: postBuildActions)
        {
            if(a.getId() == id)
            {
                return a;
            }
        }

        return null;
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
