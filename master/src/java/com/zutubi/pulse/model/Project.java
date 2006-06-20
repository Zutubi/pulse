package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
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

    /**
     * The checkout scheme defines the maner in which a projects source is bootstrapped.
     *
     */
    public enum CheckoutScheme
    {
        /**
         * Always checkout a fresh copy of the project
         */
        CHECKOUT_ONLY,

        /**
         * Keep a local copy of the project and update it to the required revision.
         */
        CHECKOUT_AND_UPDATE
    }

    public static final int DEFAULT_WORK_DIR_BUILDS = 10;

    private String name;
    private String description;
    private String url;
    private PulseFileDetails pulseFileDetails;
    private List<PostBuildAction> postBuildActions = new LinkedList<PostBuildAction>();
    private List<CleanupRule> cleanupRules = new LinkedList<CleanupRule>();
    private Scm scm;
    private State state = State.IDLE;

    private CheckoutScheme checkoutScheme = CheckoutScheme.CHECKOUT_ONLY;

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

        aclEntries = new LinkedList<ProjectAclEntry>();
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
        copy.checkoutScheme = checkoutScheme;
        copy.buildSpecifications = new LinkedList<BuildSpecification>();
        for(BuildSpecification spec: buildSpecifications)
        {
            copy.buildSpecifications.add(spec.copy());
        }

        copy.aclEntries = new LinkedList<ProjectAclEntry>();
        for(ProjectAclEntry acl: aclEntries)
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


    /**
     * The checkout scheme defines the maner in which this project is bootstrapped. The
     * default checkout scheme is CHECKOUT_ONLY.
     *
     * @return this projects configured checkout scheme.
     *
     * @see CheckoutScheme#CHECKOUT_AND_UPDATE
     * @see CheckoutScheme#CHECKOUT_ONLY
     */
    public CheckoutScheme getCheckoutScheme()
    {
        if (checkoutScheme == null)
        {
            checkoutScheme = CheckoutScheme.CHECKOUT_ONLY;
        }
        return checkoutScheme;
    }

    /**
     *
     * @param scheme
     *
     * @throws IllegalArgumentException if you set the scheme to be CHECKOUT_AND_UPDATE
     * when the configured SCM does not support the update operation. Be sure to check
     * the SCM.
     */
    public void setCheckoutScheme(CheckoutScheme scheme)
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Checkout scheme can not be null.");
        }
        // ensure that if the checkout scheme is update, that it is supported
        // by the configured scm.
        if (scheme == CheckoutScheme.CHECKOUT_AND_UPDATE && !getScm().supportsUpdate())
        {
            throw new IllegalArgumentException("Checkout and Update scheme not supported " +
                    "by the scm of this project.");
        }

        this.checkoutScheme = scheme;
    }

    /**
     * Used by hibernate to persist the checkout scheme value.
     */
    private String getCheckoutSchemeName()
    {
        return checkoutScheme.toString();
    }

    /**
     * Used by hibernate to persist the checkout scheme value.
     */
    private void setCheckoutSchemeName(String str)
    {
        // need to handle nulls in the database.
        if (TextUtils.stringSet(str))
        {
            checkoutScheme = CheckoutScheme.valueOf(str);
        }
        else
        {
            checkoutScheme = CheckoutScheme.CHECKOUT_ONLY;
        }
    }

    public AclObjectIdentity getAclObjectIdentity()
    {
        return this;
    }

    public List<ProjectAclEntry> getAclEntries()
    {
        return aclEntries;
    }

    private void setAclEntries(List<ProjectAclEntry> aclEntries)
    {
        this.aclEntries = aclEntries;
    }

    public boolean hasAdmin(String login)
    {
        for(ProjectAclEntry acl: aclEntries)
        {
            if(acl.getRecipient().equals(login))
            {
                return true;
            }
        }

        return false;
    }

    public void addAdmin(String login)
    {
        if(!hasAdmin(login))
        {
            aclEntries.add(new ProjectAclEntry(login, this, ProjectAclEntry.WRITE));
        }
    }

    public void removeAdmin(String login)
    {
        ProjectAclEntry remove = null;
        for(ProjectAclEntry entry: aclEntries)
        {
            if(entry.getRecipient().equals(login))
            {
                remove = entry;
                break;
            }
        }

        if(remove != null)
        {
            aclEntries.remove(remove);
        }
    }
}
