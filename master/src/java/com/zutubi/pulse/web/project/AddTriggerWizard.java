/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class AddTriggerWizard extends BaseWizard
{
    private static final String MONITOR_STATE = "monitor";
    private static final String CRON_STATE = "cron";

    private static final Logger LOG = Logger.getLogger(AddTriggerWizard.class);

    private long projectId;

    private ProjectManager projectManager;
    private Scheduler scheduler;

    private SelectTriggerType selectState;
    private ConfigureCronTrigger configCron;
    private WizardCompleteState finalState;

    public AddTriggerWizard()
    {
        selectState = new SelectTriggerType(this, "select");
        configCron = new ConfigureCronTrigger(this, CRON_STATE);
        finalState = new WizardCompleteState(this, "success");

        addInitialState("select", selectState);
        addState(configCron);
        addFinalState("success", finalState);
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

        BuildSpecification buildSpec = projectManager.getBuildSpecification(selectState.getSpec());

        Trigger trigger = null;
        if (CRON_STATE.equals(selectState.getType()))
        {
            trigger = new CronTrigger(configCron.cron, selectState.getName(), project.getName());
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, buildSpec.getId());
        }
        else if (MONITOR_STATE.equals(selectState.getType()))
        {
            trigger = new EventTrigger(SCMChangeEvent.class, selectState.getName(), project.getName(), SCMChangeEventFilter.class);
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, buildSpec.getId());
        }

        trigger.setProject(project.getId());
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.getDataMap().put(BuildProjectTask.PARAM_PROJECT, project.getId());

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
        private String spec;
        private List<String> specs;

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

        public String getSpec()
        {
            return spec;
        }

        public void setSpec(String spec)
        {
            this.spec = spec;
        }

        public Map<String, String> getTypes()
        {
            return types;
        }

        public List<String> getSpecs()
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

            specs = new LinkedList<String>();
            for (BuildSpecification spec : project.getBuildSpecifications())
            {
                specs.add(spec.getName());
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
}