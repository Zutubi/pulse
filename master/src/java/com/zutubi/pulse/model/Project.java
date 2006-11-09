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

    public static final int DEFAULT_WORK_DIR_BUILDS = 10;

    private String name;
    private String description;
    private String url;
    private PulseFileDetails pulseFileDetails;
    private List<PostBuildAction> postBuildActions = new LinkedList<PostBuildAction>();
    private List<CleanupRule> cleanupRules = new LinkedList<CleanupRule>();
    private Scm scm;
    private ChangeViewer changeViewer;
    private State state = State.IDLE;
    private long nextBuildNumber = 1;

    private List<BuildSpecification> buildSpecifications;

    private List<ProjectAclEntry> aclEntries;

    public Project()
    {
    }

    public Project(String name, String description)
    {
        this(name, description, new VersionedPulseFileDetails("pulse.xml"));
    }

    public Project(String name, String description, PulseFileDetails pulseFileDetails)
    {
        this.name = name;
        this.description = description;
        this.pulseFileDetails = pulseFileDetails;
        this.addCleanupRule(new CleanupRule(true, null, DEFAULT_WORK_DIR_BUILDS, CleanupRule.CleanupUnit.BUILDS));
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
        copy.cleanupRules = new LinkedList<CleanupRule>();
        for(CleanupRule rule: cleanupRules)
        {
            copy.cleanupRules.add(rule.copy());
        }

        copy.scm = scm.copy();
        if(changeViewer != null)
        {
            copy.changeViewer = changeViewer.copy();
        }

        copy.buildSpecifications = new LinkedList<BuildSpecification>();
        for(BuildSpecification spec: buildSpecifications)
        {
            copy.buildSpecifications.add(spec.copy());
        }

        copy.postBuildActions = new LinkedList<PostBuildAction>();
        for(PostBuildAction action: postBuildActions)
        {
            copy.postBuildActions.add(action.copy());
        }

        // Fix the build specification references. The copied post build actions currently reference the original
        // projects specifications. Since the specifications have themselves been copied, we need to reference the
        // new copies.
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

    public Scm getScm()
    {
        return scm;
    }

    public void setScm(Scm scm)
    {
        this.scm = scm;
    }

    public ChangeViewer getChangeViewer()
    {
        return changeViewer;
    }

    public void setChangeViewer(ChangeViewer changeViewer)
    {
        this.changeViewer = changeViewer;
    }

    public List<BuildSpecification> getBuildSpecifications()
    {
        if (buildSpecifications == null)
        {
            buildSpecifications = new LinkedList<BuildSpecification>();
        }

        return buildSpecifications;
    }

    public void addBuildSpecification(BuildSpecification specification)
    {
        getBuildSpecifications().add(specification);
    }

    private void setBuildSpecifications(List<BuildSpecification> buildSpecifications)
    {
        this.buildSpecifications = buildSpecifications;
    }

    public BuildSpecification getBuildSpecification(long id)
    {
        for(BuildSpecification s: buildSpecifications)
        {
            if(s.getId() == id)
            {
                return s;
            }
        }

        return null;
    }

    public BuildSpecification getBuildSpecification(String name)
    {
        for (BuildSpecification spec : buildSpecifications)
        {
            if (spec.getName().compareToIgnoreCase(name) == 0)
            {
                return spec;
            }
        }
        return null;
    }

    public boolean remove(BuildSpecification buildSpecification)
    {
        return buildSpecifications.remove(buildSpecification);
    }

    public List<Long> getBuildSpecificationIds()
    {
        List<Long> ids = new LinkedList<Long>();
        for(BuildSpecification spec: buildSpecifications)
        {
            ids.add(spec.getId());
        }

        return ids;
    }

    public List<BuildSpecification> lookupBuildSpecifications(List<Long> ids)
    {
        List<BuildSpecification> result = new LinkedList<BuildSpecification>();
        if (ids != null)
        {
            for(Long id: ids)
            {
                BuildSpecification buildSpecification = getBuildSpecification(id);
                if(buildSpecification != null)
                {
                    result.add(buildSpecification);
                }
            }
        }

        return result;
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

    public List<CleanupRule> getCleanupRules()
    {
        return cleanupRules;
    }

    private void setCleanupRules(List<CleanupRule> cleanupRules)
    {
        this.cleanupRules = cleanupRules;
    }

    public void addCleanupRule(CleanupRule rule)
    {
        cleanupRules.add(rule);
    }

    public CleanupRule getCleanupRule(long id)
    {
        for (CleanupRule rule : cleanupRules)
        {
            if (rule.getId() == id)
            {
                return rule;
            }
        }

        return null;
    }

    public void removeCleanupRule(long id)
    {
        CleanupRule deadRuleWalking = getCleanupRule(id);
        if (deadRuleWalking != null)
        {
            cleanupRules.remove(deadRuleWalking);
        }
    }

    public State getState()
    {
        return state;
    }

    private String getStateName()
    {
        return state.toString();
    }

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

    public long getNextBuildNumber()
    {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(long nextBuildNumber)
    {
        this.nextBuildNumber = nextBuildNumber;
    }
}
