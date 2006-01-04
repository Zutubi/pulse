package com.cinnamonbob.web.project;

import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.DefaultScheduler;
import com.cinnamonbob.scheduling.CronTrigger;
import com.cinnamonbob.scheduling.SchedulingException;
import com.cinnamonbob.scheduling.tasks.BuildProjectTask;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.util.TextUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class AddTriggerWizard extends BaseWizard
{
    private static final Logger LOG = Logger.getLogger(AddTriggerWizard.class);

    private ProjectManager projectManager;
    private DefaultScheduler scheduler;

    public AddTriggerWizard()
    {
        SelectTriggerType select = new SelectTriggerType(this, "select");
        setCurrentState(select);
        addState(select);
        addState(new ConfigureCronTrigger(this, "cron"));
        addState(new ConfigureMonitorTrigger(this, "monitor"));
    }

    public void process()
    {
        // wizard is finished, now we create the appropriate trigger.
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScheduler(DefaultScheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public class SelectTriggerType extends BaseWizardState
    {
        private Map<String, String> types;

        private long project;

        private String type;

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
                types.put("monitor", "Monitor SCM Trigger");
                types.put("cron", "Cron Trigger");
            }
            return types;
        }

        public void validate()
        {
            if (!TextUtils.stringSet(type) || !types.containsKey(type))
            {
                addFieldError("state.type", "Invalid type '" + type + "' specified. ");
            }
        }

        public SelectTriggerType(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextState()
        {
            return type;
        }
    }

    public class ConfigureCronTrigger extends BaseWizardState
    {
        private String name;
        private String recipe;
        private String cron;

        public ConfigureCronTrigger(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public void execute()
        {
            long id = ((SelectTriggerType) getState("select")).getProject();

            Project project = projectManager.getProject(id);

            CronTrigger trigger = new CronTrigger(cron, name);
            trigger.setProject(project.getId());
            trigger.setTaskClass(BuildProjectTask.class);
            trigger.getDataMap().put(BuildProjectTask.PARAM_PROJECT, project.getId());
            trigger.getDataMap().put(BuildProjectTask.PARAM_RECIPE, recipe);

            try
            {
                scheduler.schedule(trigger);
            }
            catch (SchedulingException e)
            {
                LOG.severe(e.getMessage(), e);
            }
        }

        public String getNextState()
        {
            return null;
        }

        public String getCron()
        {
            return cron;
        }

        public void setCron(String cron)
        {
            this.cron = cron;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getRecipe()
        {
            return recipe;
        }

        public void setRecipe(String recipe)
        {
            this.recipe = recipe;
        }
    }

    public class ConfigureMonitorTrigger extends BaseWizardState
    {
        public ConfigureMonitorTrigger(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextState()
        {
            return null;
        }
    }
}