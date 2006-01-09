package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.scheduling.*;
import com.cinnamonbob.scheduling.tasks.BuildProjectTask;
import com.cinnamonbob.scm.SCMChangeEvent;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.opensymphony.util.TextUtils;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class AddTriggerWizard extends BaseWizard
{
    private static final String MONITOR_STATE = "monitor";
    private static final String CRON_STATE = "cron";

    private static final Logger LOG = Logger.getLogger(AddTriggerWizard.class);

    private ProjectManager projectManager;
    private Scheduler scheduler;

    private SelectTriggerType selectState;
    private ConfigureCronTrigger configCron;
    private ConfigureMonitorTrigger configMonitor;

    public AddTriggerWizard()
    {
        selectState = new SelectTriggerType(this, "select");
        configCron = new ConfigureCronTrigger(this, "cron");
        configMonitor = new ConfigureMonitorTrigger(this, "monitor");

        setCurrentState(selectState);

        addState(selectState);
        addState(configCron);
        addState(configMonitor);
    }

    public long getProject()
    {
        return selectState.getProject();
    }

    public void process()
    {
        // wizard is finished, now we create the appropriate trigger.

        Project project = projectManager.getProject(getProject());

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

        private long project;

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

        public long getProject()
        {
            return project;
        }

        public void setProject(long project)
        {
            this.project = project;
        }

        public Map<String, String> getTypes()
        {
            if (types == null)
            {
                types = new TreeMap<String, String>();
                types.put(MONITOR_STATE, "monitor scm tigger");
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

        public String getNextState()
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

        public String getNextState()
        {
            return null;
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
            long projectId = ((AddTriggerWizard) getWizard()).getProject();
            Project project = projectManager.getProject(projectId);
            if(project == null)
            {
                addActionError("Unknown project '" + projectId + "'");
                return;
            }

            specs = new LinkedList<String>();
            for(BuildSpecification spec: project.getBuildSpecifications())
            {
                specs.add(spec.getName());
            }

            if(specs.size() == 0)
            {
                addActionError("No build specifications for project '" + project.getName() + "'");
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