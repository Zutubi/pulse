package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.model.CheckboxFieldDescriptor;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.pulse.master.tove.webwork.ConfigurationPanel;
import com.zutubi.pulse.master.tove.webwork.ConfigurationResponse;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
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

public class EditBuildPropertiesAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(EditBuildPropertiesAction.class);

    private static final String SUBMIT_TRIGGER = "trigger";

    private static final String PROPERTY_PREFIX = "property.";

    private String formSource;
    private String revision;
    private List<ResourcePropertyConfiguration> properties;
    private String status;
    private String version;
    private String priority;
    private boolean rebuild;
    private boolean ajax;
    private ConfigurationPanel newPanel;
    private ConfigurationResponse configurationResponse;
    private String submitField;

    private ActionManager actionManager;
    private ScmManager scmManager;
    private Configuration configuration;

    public boolean isCancelled()
    {
        return "cancel".equals(submitField);
    }

    public String getFormSource()
    {
        return formSource;
    }

    public List<ResourcePropertyConfiguration> getProperties()
    {
        return properties;
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

    public ConfigurationPanel getNewPanel()
    {
        return newPanel;
    }

    public ConfigurationResponse getConfigurationResponse()
    {
        return configurationResponse;
    }

    private void renderForm() throws IOException, TemplateException
    {
        Project project = getRequiredProject();
        properties = new ArrayList<ResourcePropertyConfiguration>(project.getConfig().getProperties().values());

        Form form = new Form("form", "edit.build.properties", (ajax ? "ajax/action/" : "") + "editBuildProperties.action", SUBMIT_TRIGGER);
        form.setAjax(ajax);

        Field field = new Field(FieldType.HIDDEN, "projectName");
        field.setValue(getProjectName());
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

        if (actionManager.getActions(project.getConfig(), false, true).contains(ProjectConfigurationActions.ACTION_REBUILD))
        {
            CheckboxFieldDescriptor rebuildFieldDescriptor = new CheckboxFieldDescriptor();
            rebuildFieldDescriptor.setName("rebuild");
            field = rebuildFieldDescriptor.instantiate(null, null);
            field.setValue(Boolean.toString(rebuild));
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

        for(ResourcePropertyConfiguration property: properties)
        {
            field = new Field(FieldType.TEXT, PROPERTY_PREFIX + property.getName());
            field.setLabel(property.getName());
            field.setValue(property.getValue());
            field.addParameter("help", property.getDescription());
            form.add(field);
        }

        addSubmit(form, SUBMIT_TRIGGER);
        addSubmit(form, "cancel");

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("projectId", project.getId());

        StringWriter writer = new StringWriter();
        ToveUtils.renderForm(context, form, getClass(), writer, configuration);
        formSource = writer.toString();
        newPanel = new ConfigurationPanel("ajax/action/edit-build-properties.vm");
    }

    private void addLatestAction(Field field, Project project, ProjectConfiguration projectConfig)
    {
        try
        {
            Set<ScmCapability> capabilities = ScmClientUtils.getCapabilities(project, projectConfig, scmManager);
            if(capabilities.contains(ScmCapability.REVISIONS))
            {
                field.addParameter(ACTIONS, Arrays.asList("getlatest"));
                field.addParameter(SCRIPTS, Arrays.asList("EditBuildPropertiesAction.getlatest"));
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
        renderForm();
        return INPUT;
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
    }

    public void doCancel()
    {
        setupResponse();
    }

    public String execute() throws IOException, TemplateException
    {
        Project project = getRequiredProject();

        Revision r = null;
        revision = revision.trim();
        if (StringUtils.stringSet(revision))
        {
            try
            {
                r = withScmClient(project.getConfig(), scmManager, new ScmContextualAction<Revision>()
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

        try
        {
            TriggerOptions options = new TriggerOptions(new ManualTriggerBuildReason(getPrinciple()), ProjectManager.TRIGGER_CATEGORY_MANUAL);
            options.setProperties(mapProperties(project.getConfig()));
            options.setStatus(status);
            options.setVersion(version);
            options.setRebuild(rebuild);
            if (StringUtils.stringSet(priority))
            {
                options.setPriority(Integer.valueOf(priority));
            }
            
            projectManager.triggerBuild(project.getConfig(), options, r);
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

    private List<ResourcePropertyConfiguration> mapProperties(ProjectConfiguration projectConfig)
    {
        List<ResourcePropertyConfiguration> properties = new LinkedList<ResourcePropertyConfiguration>();
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(PROPERTY_PREFIX))
            {
                String propertyName = name.substring(PROPERTY_PREFIX.length());
                ResourcePropertyConfiguration property = projectConfig.getProperty(propertyName);
                if(property != null)
                {
                    property = property.copy();
                    Object value = parameters.get(name);
                    if(value instanceof String)
                    {
                        property.setValue((String) value);
                    }
                    else if(value instanceof String[])
                    {
                        property.setValue(((String[])value)[0]);
                    }

                    properties.add(property);
                }
            }
        }

        return properties;
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
}
