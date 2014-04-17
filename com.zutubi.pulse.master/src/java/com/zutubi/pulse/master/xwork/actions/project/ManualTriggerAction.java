package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.build.queue.graph.BuildGraphData;
import com.zutubi.pulse.master.build.queue.graph.GraphBuilder;
import com.zutubi.pulse.master.build.queue.graph.GraphFilters;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.pulse.master.tove.model.CheckboxFieldDescriptor;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.pulse.master.tove.webwork.ConfigurationPanel;
import com.zutubi.pulse.master.tove.webwork.ConfigurationResponse;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.TreeNode;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import static com.zutubi.tove.annotations.FieldParameter.ACTIONS;
import static com.zutubi.tove.annotations.FieldParameter.SCRIPTS;

public class ManualTriggerAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(ManualTriggerAction.class);

    private static final String SUBMIT_TRIGGER = "trigger";

    private static final String PROJECT_PROPERTY_PREFIX = "pproperty.";
    private static final String TRIGGER_PROPERTY_PREFIX = "tproperty.";

    private long triggerHandle;
    private ManualTriggerConfiguration triggerConfig;

    private String formSource;
    private String revision;
    private String status;
    private String version;
    private String priority;
    private boolean rebuild;
    private boolean ajax;
    private SimpleResult result;
    private ConfigurationPanel newPanel;
    private ConfigurationResponse configurationResponse;
    private String submitField;

    private ActionManager actionManager;
    private ScmManager scmManager;
    private Configuration configuration;
    private ConfigurationProvider configurationProvider;

    public long getTriggerHandle()
    {
        return triggerHandle;
    }

    public void setTriggerHandle(long triggerHandle)
    {
        this.triggerHandle = triggerHandle;
    }

    public boolean isCancelled()
    {
        return "cancel".equals(submitField);
    }

    public String getFormSource()
    {
        return formSource;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public boolean isRebuild()
    {
        return rebuild;
    }

    public void setRebuild(boolean rebuild)
    {
        this.rebuild = rebuild;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public void setPath(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if(elements.length == 2 && elements[0].equals(MasterConfigurationRegistry.PROJECTS_SCOPE))
        {
            setProjectName(elements[1]);
        }
    }

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }

    public void setSubmitField(String submitField)
    {
        this.submitField = submitField;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public ConfigurationPanel getNewPanel()
    {
        return newPanel;
    }

    public ConfigurationResponse getConfigurationResponse()
    {
        return configurationResponse;
    }

    private ManualTriggerConfiguration getTriggerConfig()
    {
        if (triggerConfig == null)
        {
            triggerConfig = lookupTriggerConfig(getRequiredProject().getConfig());
        }

        return triggerConfig;
    }

    private ManualTriggerConfiguration lookupTriggerConfig(ProjectConfiguration projectConfig)
    {
        List<ManualTriggerConfiguration> manualTriggers = TriggerUtils.getTriggers(projectConfig, ManualTriggerConfiguration.class);
        for (ManualTriggerConfiguration trigger: manualTriggers)
        {
            if (trigger.getHandle() == triggerHandle)
            {
                return trigger;
            }
        }

        throw new LookupErrorException("Trigger invalid or unspecified");
    }

    private HashMap<String, ResourcePropertyConfiguration> getMergedProperties(ProjectConfiguration project, ManualTriggerConfiguration trigger)
    {
        HashMap<String, ResourcePropertyConfiguration> propertyMap = new LinkedHashMap<String, ResourcePropertyConfiguration>();
        propertyMap.putAll(project.getProperties());
        propertyMap.putAll(trigger.getProperties());
        return propertyMap;
    }

    private void renderForm() throws IOException, TemplateException
    {
        Project project = getRequiredProject();
        ManualTriggerConfiguration triggerConfig = getTriggerConfig();

        Form form = new Form("form", "edit.build.properties", (ajax ? "ajax/action/" : "") + "manualTrigger.action", SUBMIT_TRIGGER);
        form.setAjax(ajax);

        Field field = new Field(FieldType.HIDDEN, "projectName");
        field.setValue(getProjectName());
        form.add(field);

        field = new Field(FieldType.HIDDEN, "triggerHandle");
        field.setValue(Long.toString(getTriggerConfig().getHandle()));
        form.add(field);

        field = new Field(FieldType.TEXT, "version");
        field.setLabel("version");
        field.setValue(project.getConfig().getDependencies().getVersion());
        form.add(field);

        OptionFieldDescriptor statusFieldDescriptor = new OptionFieldDescriptor();
        statusFieldDescriptor.setName("status");
        statusFieldDescriptor.setType(FieldType.TEXT);
        statusFieldDescriptor.setList(IvyStatus.getStatuses());
        MutableRecord r = new MutableRecordImpl();
        r.put("status", project.getConfig().getDependencies().getStatus());
        form.add(statusFieldDescriptor.instantiate(null, r));

        if (hasDependencyOfBuildableStatus(project.getConfig()))
        {
            CheckboxFieldDescriptor rebuildFieldDescriptor = new CheckboxFieldDescriptor();
            rebuildFieldDescriptor.setName("rebuild");
            field = rebuildFieldDescriptor.instantiate(null, null);
            field.setValue(Boolean.toString(triggerConfig.isRebuildUpstreamDependencies()));
            form.add(field);
        }

        field = new Field(FieldType.TEXT, "revision");
        field.setLabel("revision");
        field.setValue(revision);
        addLatestAction(field, project, project.getConfig());
        form.add(field);

        field = new Field(FieldType.TEXT, "priority");
        field.setLabel("priority");
        field.setValue(priority);
        form.add(field);

        Map<String, ResourcePropertyConfiguration> triggerProperties = triggerConfig.getProperties();
        for (ResourcePropertyConfiguration property: project.getConfig().getProperties().values())
        {
            if (!triggerProperties.containsKey(property.getName()))
            {
                addPropertyField(form, property, PROJECT_PROPERTY_PREFIX);
            }
        }

        for (ResourcePropertyConfiguration property : triggerProperties.values())
        {
            addPropertyField(form, property, TRIGGER_PROPERTY_PREFIX);
        }

        addSubmit(form, SUBMIT_TRIGGER);
        addSubmit(form, "cancel");

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("projectId", project.getId());

        StringWriter writer = new StringWriter();
        ToveUtils.renderForm(context, form, getClass(), writer, configuration);
        formSource = writer.toString();
        newPanel = new ConfigurationPanel("ajax/action/manual-trigger.vm");
    }

    private void addPropertyField(Form form, ResourcePropertyConfiguration property, String prefix)
    {
        Field field = new Field(FieldType.TEXT, prefix + property.getName());
        field.setLabel(property.getName());
        field.setValue(property.getValue());
        field.addParameter("help", property.getDescription());
        form.add(field);
    }

    private boolean hasDependencyOfBuildableStatus(ProjectConfiguration projectConfig)
    {
        if (projectConfig.hasDependencies())
        {
            String ourStatus = projectConfig.getDependencies().getStatus();
            GraphBuilder builder = objectFactory.buildBean(GraphBuilder.class);
            GraphFilters filters = objectFactory.buildBean(GraphFilters.class);
            TreeNode<BuildGraphData> upstream = builder.buildUpstreamGraph(projectConfig,
                    filters.status(ourStatus),
                    filters.transitive(),
                    filters.duplicate());
            return upstream.getChildren().size() > 0;
        }

        return false;
    }

    private void addLatestAction(Field field, Project project, ProjectConfiguration projectConfig)
    {
        try
        {
            Set<ScmCapability> capabilities = ScmClientUtils.getCapabilities(projectConfig, project.getState(), scmManager);
            if(capabilities.contains(ScmCapability.REVISIONS))
            {
                field.addParameter(ACTIONS, Arrays.asList("getlatest"));
                field.addParameter(SCRIPTS, Arrays.asList("ManualTriggerAction.getlatest"));
            }
        }
        catch (ScmException e)
        {
            // Just don't add the action.
        }
    }

    private void addSubmit(Form form, String name)
    {
        Field field = new Field(FieldType.SUBMIT, name);
        field.setValue(name);
        form.add(field);
    }

    public String doInput() throws Exception
    {
        ManualTriggerConfiguration triggerConfig = getTriggerConfig();
        if (triggerConfig.isPrompt())
        {
            renderForm();
            return INPUT;
        }
        else
        {
            return execute();
        }
    }

    private String getPath()
    {
        return PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, getProjectName());
    }

    public String getProjectPath()
    {
        return getPath();
    }

    private void setupResponse()
    {
        String newPath = getPath();
        configurationResponse = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
        result = new SimpleResult(true, "project build requested");
    }

    public void doCancel()
    {
        setupResponse();
    }

    public String execute() throws IOException, TemplateException
    {
        Project project = getRequiredProject();
        ManualTriggerConfiguration triggerConfig = getTriggerConfig();

        Revision r = null;
        TriggerOptions options = new TriggerOptions(new ManualTriggerBuildReason(getPrinciple()), ProjectManager.TRIGGER_CATEGORY_MANUAL);
        ProjectConfiguration projectConfig = project.getConfig();
        if (triggerConfig.isPrompt())
        {
            revision = revision.trim();
            if (StringUtils.stringSet(revision))
            {
                try
                {
                    r = withScmClient(projectConfig, project.getState(), scmManager, new ScmContextualAction<Revision>()
                    {
                        public Revision process(ScmClient client, ScmContext context) throws ScmException
                        {
                            return client.parseRevision(context, revision);
                        }
                    });
                }
                catch (ScmException e)
                {
                    addFieldError("revision", "Unable to verify revision: " + e.getMessage());
                    LOG.severe(e);
                    renderForm();
                    return INPUT;
                }
            }

            // We apply properties to the configuration used for the build so they will be access
            // and applied in the same way regardless of whether they came from a prompt.  This
            // maintains property precedence and avoids duplication of the properties in the scope
            // (which can cause issues like CIB-3090).
            projectConfig = configurationProvider.deepClone(projectConfig);
            triggerConfig = lookupTriggerConfig(projectConfig);
            mapProperties(projectConfig, triggerConfig);

            options.setStatus(status);
            options.setVersion(version);
            options.setRebuild(rebuild);
            if (StringUtils.stringSet(priority))
            {
                options.setPriority(Integer.valueOf(priority));
            }
        }
        else
        {
            options.setRebuild(triggerConfig.isRebuildUpstreamDependencies());
        }

        options.setProperties(triggerConfig.getProperties().values());

        try
        {
            projectManager.triggerBuild(projectConfig, options, r);
        }
        catch (Exception e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }

        pauseForDramaticEffect();
        setupResponse();
        return SUCCESS;
    }

    private void mapProperties(ProjectConfiguration projectConfig, ManualTriggerConfiguration triggerConfig)
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if (name.startsWith(PROJECT_PROPERTY_PREFIX))
            {
                String propertyName = name.substring(PROJECT_PROPERTY_PREFIX.length());
                updateProperty(projectConfig.getProperty(propertyName), parameters, name);
            }
            else if (name.startsWith(TRIGGER_PROPERTY_PREFIX))
            {
                String propertyName = name.substring(TRIGGER_PROPERTY_PREFIX.length());
                updateProperty(triggerConfig.getProperties().get(propertyName), parameters, name);
            }
        }
    }

    private void updateProperty(ResourcePropertyConfiguration property, Map parameters, String name)
    {
        if (property != null)
        {
            Object value = parameters.get(name);
            if(value instanceof String)
            {
                property.setValue((String) value);
            }
            else if(value instanceof String[])
            {
                property.setValue(((String[])value)[0]);
            }
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
