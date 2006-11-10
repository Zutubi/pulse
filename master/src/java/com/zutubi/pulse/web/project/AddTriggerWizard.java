package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.scheduling.*;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardCompleteState;

import java.util.*;

/**
 * <class-comment/>
 */
public class AddTriggerWizard extends BaseWizard
{
    private static final String MONITOR_STATE = "monitor";
    private static final String BUILD_COMPLETED_STATE = "build.completed";
    private static final String CRON_STATE = "cron";

    private static final Logger LOG = Logger.getLogger(AddTriggerWizard.class);

    private long projectId;

    private ProjectManager projectManager;
    private Scheduler scheduler;

    private SelectTriggerType selectState;
    private ConfigureCronTrigger configCron;
    private ConfigureBuildCompletedTrigger configBuildCompleted;

    public AddTriggerWizard()
    {
        selectState = new SelectTriggerType(this, "select");
        configCron = new ConfigureCronTrigger(this, CRON_STATE);
        configBuildCompleted = new ConfigureBuildCompletedTrigger(this, BUILD_COMPLETED_STATE);

        addInitialState(selectState);
        addState(configCron);
        addState(configBuildCompleted);
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId);
    }

    public void process()
    {
        // wizard is finished, now we create the appropriate trigger.

        Project project = projectManager.getProject(getProjectId());

        Trigger trigger = null;
        boolean force = true;
        if (CRON_STATE.equals(selectState.getType()))
        {
            trigger = new CronTrigger(configCron.cron, selectState.getName(), project.getName());
        }
        else if (MONITOR_STATE.equals(selectState.getType()))
        {
            trigger = new EventTrigger(SCMChangeEvent.class, selectState.getName(), project.getName(), SCMChangeEventFilter.class);
            force = false;
        }
        else if (BUILD_COMPLETED_STATE.equals(selectState.getType()))
        {
            trigger = new EventTrigger(BuildCompletedEvent.class, selectState.getName(), project.getName(), BuildCompletedEventFilter.class);
            configBuildCompleted.getHelper().populateTrigger(trigger, configBuildCompleted.getFilterProject(), configBuildCompleted.getFilterSpecification(), configBuildCompleted.getFilterStateNames());
        }

        trigger.setProject(project.getId());
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, selectState.getSpec());
        if(force)
        {
            trigger.getDataMap().put(BuildProjectTask.PARAM_FORCE, true);
        }

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            addActionError(e.getMessage());
            LOG.severe(e.getMessage(), e);
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public class SelectTriggerType extends BaseWizardState implements Validateable
    {
        private Map<String, String> types;
        private String name;
        private long spec;
        private Map<Long, String> specs;

        private String type;

        public SelectTriggerType(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public long getSpec()
        {
            return spec;
        }

        public void setSpec(long spec)
        {
            this.spec = spec;
        }

        public Map<String, String> getTypes()
        {
            return types;
        }

        public Map<Long, String> getSpecs()
        {
            return specs;
        }

        public void validate()
        {
            if (!TextUtils.stringSet(type) || !types.containsKey(type))
            {
                addFieldError("type", "invalid type '" + type + "' specified. ");
                return;
            }

            // ensure that the selected name is not already in use for this project.
            long projectId = ((AddTriggerWizard) getWizard()).getProjectId();
            if (scheduler.getTrigger(projectId, name) != null)
            {
                addFieldError("name", "the name " + name + " is already being used. please use a different name.");
            }
        }

        @Override
        public void initialise()
        {
            super.initialise();

            long projectId = ((AddTriggerWizard) getWizard()).getProjectId();
            Project project = projectManager.getProject(projectId);
            if (project == null)
            {
                addActionError("Unknown projectId '" + projectId + "'");
                return;
            }

            specs = new LinkedHashMap<Long, String>();
            for (BuildSpecification spec : project.getBuildSpecifications())
            {
                specs.put(spec.getId(), spec.getName());
            }

            if (specs.size() == 0)
            {
                addActionError("No build specifications for projectId '" + project.getName() + "'");
            }

            if (types == null)
            {
                types = new TreeMap<String, String>();
                types.put(MONITOR_STATE, "monitor scm trigger");
                types.put(CRON_STATE, "cron trigger");
                types.put(BUILD_COMPLETED_STATE, "build completed");
            }

        }

        public String getNextStateName()
        {
            if (TextUtils.stringSet(type))
            {
                if (MONITOR_STATE.equals(type))
                {
                    return "success";
                }
                return type;
            }
            return super.getStateName();
        }
    }

    public class ConfigureCronTrigger extends BaseWizardState
    {
        private String cron;

        public ConfigureCronTrigger(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public String getCron()
        {
            return cron;
        }

        public void setCron(String cron)
        {
            this.cron = cron;
        }

        public String getNextStateName()
        {
            return "success";
        }
    }

    public class ConfigureBuildCompletedTrigger extends BaseWizardState
    {
        private BuildCompletedTriggerHelper helper = new BuildCompletedTriggerHelper();
        private Long filterProject;
        private String filterSpecification;
        private List<String> filterStateNames;

        public ConfigureBuildCompletedTrigger(Wizard wizard, String stateName)
        {
            super(wizard, stateName);
        }

        public void initialise()
        {
            helper.initialise(projectManager);
        }

        public Map<Long, String> getFilterProjects()
        {
            return helper.getFilterProjects();
        }

        public Map<Long, List<String>> getFilterSpecifications()
        {
            return helper.getFilterSpecifications();
        }

        public Map<String, String> getStateMap()
        {
            return helper.getStateMap();
        }

        public Long getFilterProject()
        {
            return filterProject;
        }

        public void setFilterProject(Long filterProject)
        {
            this.filterProject = filterProject;
        }

        public String getFilterSpecification()
        {
            return filterSpecification;
        }

        public void setFilterSpecification(String filterSpecification)
        {
            this.filterSpecification = filterSpecification;
        }

        public List<String> getFilterStateNames()
        {
            return filterStateNames;
        }

        public void setFilterStateNames(List<String> filterStateNames)
        {
            this.filterStateNames = filterStateNames;
        }

        public String getNextStateName()
        {
            return "success";
        }

        public BuildCompletedTriggerHelper getHelper()
        {
            return helper;
        }
    }
}