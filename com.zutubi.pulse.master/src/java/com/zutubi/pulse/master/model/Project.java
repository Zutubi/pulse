package com.zutubi.pulse.master.model;

import com.google.common.collect.ImmutableMap;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.EnumUtils;

import java.util.Map;

/**
 * Represents state information for a concrete Project.  Tied to a concrete {@link com.zutubi.pulse.master.tove.config.project.ProjectConfiguration}
 * instance.
 *
 * @see com.zutubi.pulse.master.tove.config.project.ProjectConfiguration#getProjectId()
 */
public class Project extends Entity implements NamedEntity
{
    /**
     * A labelling for all project state transitions.
     */
    public enum Transition
    {
        /**
         * Transition made for every project on startup.  Used to establish a
         * clean state after an unclean shutdown.
         */
        STARTUP,
        /**
         * Transition used to start project initialisation.
         */
        INITIALISE,
        /**
         * Transition when project initialisation succeeds.
         */
        INITIALISE_SUCCESS,
        /**
         * Transition when project initialisation fails.
         */
        INITIALISE_FAILURE,
        /**
         * Transition when a request is made to pause the project.
         */
        PAUSE,
        /**
         * Transition when a request is made to resume a paused project.
         */
        RESUME,
        /**
         * Transition when a project moves from idle to running one or more
         * builds.
         */
        BUILDING,
        /**
         * Transition when a project moves from running one or more builds to
         * idle.
         */
        IDLE,
        /**
         * Transition when a request is made to destroy a project, i.e. clean
         * up any initialisation artifacts.
         */
        CLEANUP,
        /**
         * Transition when a project destruction is complete.
         */
        CLEANED,
        /**
         * Transition when a request is made to delete a project.
         */
        DELETE
    }

    public enum State
    {
        /**
         * The initial state, for a just-created project before any
         * initialisation has been triggered.  In the normal case
         * initialisation is triggered as soon as the project is created, but
         * projects without an SCM will stay in this state until an SCM is
         * added.
         */
        INITIAL
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return false;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, INITIAL,
                            Transition.INITIALISE, INITIALISING,
                            Transition.DELETE, DELETING
                    );
                }

                return validTransitions;
            }
        },
        /**
         * State during initialisation.  This could be for a new project or due
         * to an explicit request to reinitialise.  Projects in this state on
         * startup are presumed to have been initialising when the server was
         * stopped and should recover by starting initialisation again.
         */
        INITIALISING
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return false;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, INITIALISATION_FAILED,
                            Transition.INITIALISE_FAILURE, INITIALISATION_FAILED,
                            Transition.INITIALISE_SUCCESS, IDLE
                    );
                }

                return validTransitions;
            }
        },
        /**
         * Initialisation failed for some reason.  The project stays in this
         * state until the user requests reinitialisation.  The project log may
         * contain more details.
         */
        INITIALISATION_FAILED
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return false;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                        Transition.STARTUP, INITIALISATION_FAILED,
                        Transition.INITIALISE, INITIALISING,
                        Transition.DELETE, DELETING
                    );
                }

                return validTransitions;
            }
        },
        /**
         * The project is currently building, when it becomes idle it will be
         * reinitialised.
         */
        INITIALISE_ON_IDLE
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return true;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, INITIALISATION_FAILED,
                            Transition.IDLE, INITIALISING,
                            Transition.CLEANUP, CLEANUP_ON_IDLE
                    );
                }

                return validTransitions;
            }
        },
        /**
         * No builds running for the project at the moment.
         */
        IDLE
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return true;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = new ImmutableMap.Builder<Transition, State>()
                            .put(Transition.STARTUP, IDLE)
                            .put(Transition.BUILDING, BUILDING)
                            .put(Transition.INITIALISE, INITIALISING)
                            .put(Transition.PAUSE, PAUSED)
                            .put(Transition.CLEANUP, CLEANING)
                            .put(Transition.DELETE, DELETING)
                            .build();
                }
                return validTransitions;
            }
        },
        /**
         * There is at least one build running for this project.
         */
        BUILDING
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return true;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return true;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, IDLE,
                            Transition.IDLE, IDLE,
                            Transition.INITIALISE, INITIALISE_ON_IDLE,
                            Transition.PAUSE, PAUSE_ON_IDLE,
                            Transition.CLEANUP, CLEANUP_ON_IDLE
                    );
                }
                return validTransitions;
            }
        },
        /**
         * Currently paused: triggers will be ignored while in this state.
         */
        PAUSED
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return personal;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, PAUSED,
                            Transition.INITIALISE, INITIALISING,
                            Transition.RESUME, IDLE,
                            Transition.CLEANUP, CLEANING,
                            Transition.DELETE, DELETING
                    );
                }

                return validTransitions;
            }
        },
        /**
         * Project is building, but will be paused when it becomes idle.
         */
        PAUSE_ON_IDLE
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return true;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return personal;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, PAUSED,
                            Transition.INITIALISE, INITIALISE_ON_IDLE,
                            Transition.IDLE, PAUSED,
                            Transition.RESUME, BUILDING,
                            Transition.CLEANUP, CLEANUP_ON_IDLE
                    );
                }

                return validTransitions;
            }
        },
        /**
         * The project's initialisation artifacts are being cleaned up because
         * its SCM configuration has been deleted.
         */
        CLEANING
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return false;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, INITIAL,
                            Transition.CLEANED, INITIAL
                    );
                }

                return validTransitions;
            }
        },
        /**
         * The project is currently building and on idle will commence move to
         * the {@link #CLEANING} state.
         */
        CLEANUP_ON_IDLE
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return true;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, IDLE,
                            Transition.IDLE, CLEANING
                    );
                }

                return validTransitions;
            }
        },
        /**
         * The final state for a project, which it can never leave.  A project
         * in this state is waiting for impending doom.
         */
        DELETING
        {
            private Map<Transition, State> validTransitions;

            public boolean isInitialised()
            {
                return true;
            }

            public boolean isBuilding()
            {
                return false;
            }

            public boolean acceptTrigger(boolean personal)
            {
                return false;
            }

            public Map<Transition, State> getValidTransitions()
            {
                if (validTransitions == null)
                {
                    validTransitions = ImmutableMap.of(
                            Transition.STARTUP, DELETING
                    );
                }

                return validTransitions;
            }
        };

        /**
         * @return true if a project in this state has been initialised
         */
        public abstract boolean isInitialised();

        /**
         * @return true if a project in this state has one or more active
         *         builds
         */
        public abstract boolean isBuilding();

        /**
         * Indicates if the project is currently accepting new build triggers.
         * A project may ignore triggers if it is not initialised, or if it
         * has been paused.
         *
         * @param personal indicates if the trigger is for a personal build
         *                 (personal builds are still allowed for paused
         *                 projects)
         * @return true if the project will accept the given type of trigger in
         *         its current state
         */
        public abstract boolean acceptTrigger(boolean personal);

        /**
         * Returns the transitions that can be made from this state, mapped to
         * the state which the transition results in.  Transitions not present
         * as keys in the map are not valid from this state.
         *
         * @return a mapping from transition to target state
         */
        public abstract Map<Transition, State> getValidTransitions();

        public String toString()
        {
            return EnumUtils.toPrettyString(this);
        }
    }

    /**
     * The state of the project, indicates what point the project has reached
     * in the lifecycle, and whether it is paused, idle or active.
     * <p/>
     * The state is guarded by the project state lock managed by the project
     * manager.  All changes to the state should thereforce be through the
     * manager.
     *
     * @see com.zutubi.pulse.master.model.ProjectManager#makeStateTransition(long, com.zutubi.pulse.master.model.Project.Transition)
     */
    private State state = State.INITIAL;

    private long nextBuildNumber = 1;
    private int buildCount = 0;
    private int successCount = 0;
    private Long lastPollTime;
    private ProjectConfiguration config;
    /**
     * If not null, holds details about the user responsible for this project.
     */
    private ProjectResponsibility responsibility;

    public Project()
    {
    }

    /**
     * For testing only.  This constructor allows the initial state of this project
     * to be set rather than needing to go through the formal state transition process.
     * 
     * @param state     the state of this project.
     */
    public Project(State state)
    {
        this.state = state;
    }

    public Long getLastPollTime()
    {
        return lastPollTime;
    }

    public void setLastPollTime(Long lastPollTime)
    {
        this.lastPollTime = lastPollTime;
    }

    /**
     * Retrieves the project's current state.  Note if anything is acted upon
     * based on this state it should be done holding the state lock exposed by
     * the project manager.
     *
     * @return the current project state
     */
    public State getState()
    {
        return state;
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Used by testing
    private void setState(State state)
    {
        this.state = state;
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Used by hibernate
    private String getStateName()
    {
        return getState().name();
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Used by hibernate
    private void setStateName(String stateName)
    {
        state = State.valueOf(stateName);
    }

    /**
     * Indicates if a given transition can be made from the current state.
     *
     * @param transition the transition to test
     * @return true iff the given transition is valid in our current state
     */
    public boolean isTransitionValid(Transition transition)
    {
        return getState().getValidTransitions().containsKey(transition);
    }

    /**
     * Performs a state transition, returning the new state.  This must only
     * be done by the project manager, which manages locking for project
     * states.
     *
     * @param transition the transition to make, must be valid in our current
     *                   state
     * @return the new state reached by making the transition
     * @throws IllegalStateException if the given transition is not valid in
     *                               our current state
     *
     * @see com.zutubi.pulse.master.model.ProjectManager#makeStateTransition(long, com.zutubi.pulse.master.model.Project.Transition) 
     */
    public State stateTransition(Transition transition)
    {
        Map<Transition, State> validTransitions = state.getValidTransitions();
        State newState = validTransitions.get(transition);
        if (newState == null)
        {
            throw new IllegalStateException("Attempt to make illegal state transition " + transition + " while in state " + state);
        }

        state = newState;
        return state;
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

    public ProjectResponsibility getResponsibility()
    {
        return responsibility;
    }

    public void setResponsibility(ProjectResponsibility responsibility)
    {
        this.responsibility = responsibility;
    }

    /**
     * @return true if the state of the project is initialised, false otherwise.
     */
    public boolean isInitialised()
    {
        return getState().isInitialised();
    }
}
