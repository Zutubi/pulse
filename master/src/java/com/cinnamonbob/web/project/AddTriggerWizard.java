package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.scheduling.*;
import com.cinnamonbob.scheduling.tasks.BuildProjectTask;
import com.cinnamonbob.scm.SCMChangeEvent;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.cinnamonbob.web.wizard.WizardCompleteState;
import com.opensymphony.util.TextUtils;

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
    private ConfigureMonitorTrigger configMonitor;
    private WizardCompleteState finalState;

    public AddTriggerWizard()
    {
        selectState = new SelectTriggerType(this, "select");
        configCron = new ConfigureCronTrigger(this, CRON_STATE);
        configMonitor = new ConfigureMonitorTrigger(this, MONITOR_STATE);
        finalState = new WizardCompleteState(this, "success");

        initialState = selectState;

        addState(selectState);
        addState(configCron);
        addState(configMonitor);
        addState(finalState);
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
        if (CRON_STATE.equals(selectState.getType()))
        {
            trigger = new CronTrigger(configCron.cron, configCron.getName());
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, configCron.getSpec());
        }
        else if (MONITOR_STATE.equals(selectState.getType()))
        {
            trigger = new EventTrigger(SCMChangeEvent.class, configMonitor.getName());
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, configMonitor.getSpec());
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

    public class SelectTriggerType extends BaseWizardState
    {
        private Map<String, String> types;

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

        public Map<String, String> getTypes()
        {
            if (types == null)
            {
                types = new TreeMap<String, String>();
                types.put(MONITOR_STATE, "monitor scm trigger");
                types.put(CRON_STATE, "cron trigger");
            }
            return types;
        }

        public void validate()
        {
            if (!TextUtils.stringSet(type) || !types.containsKey(type))
            {
                addFieldError("type", "Invalid type '" + type + "' specified. ");
            }
        }

        public String getNextStateName()
        {
            if (TextUtils.stringSet(type))
            {
                return type;
            }
            return super.getStateName();
        }
    }

    public abstract class BaseConfigureTrigger extends BaseWizardState
    {
        private String name;
        private String spec;
        private List<String> specs;

        public BaseConfigureTrigger(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return "success";
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

        @Override
        public void initialise()
        {
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
        }

        public List<String> getSpecs()
        {
            return specs;
        }

    }

    public class ConfigureCronTrigger extends BaseConfigureTrigger
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
    }

    public class ConfigureMonitorTrigger extends BaseConfigureTrigger
    {
        public ConfigureMonitorTrigger(AddTriggerWizard wizard, String stateName)
        {
            super(wizard, stateName);
        }
    }
}