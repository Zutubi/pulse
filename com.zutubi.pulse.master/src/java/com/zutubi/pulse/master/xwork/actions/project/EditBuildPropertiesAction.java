package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.webwork.ConfigurationPanel;
import com.zutubi.pulse.master.tove.webwork.ConfigurationResponse;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import static com.zutubi.tove.annotations.FieldParameter.ACTIONS;
import static com.zutubi.tove.annotations.FieldParameter.SCRIPTS;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class EditBuildPropertiesAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(EditBuildPropertiesAction.class);

    private static final String SUBMIT_TRIGGER = "trigger";

    private static final String PROPERTY_PREFIX = "property.";

    private String formSource;
    private String revision;
    private List<ResourcePropertyConfiguration> properties;
    private boolean ajax;
    private ConfigurationPanel newPanel;
    private ConfigurationResponse configurationResponse;
    private String submitField;

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

    public void setPath(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if(elements.length == 2 && elements[0].equals(ConfigurationRegistry.PROJECTS_SCOPE))
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
        Collections.sort(properties, new NamedConfigurationComparator());

        Form form = new Form("form", "edit.build.properties", (ajax ? "aaction/" : "") + "editBuildProperties.action", SUBMIT_TRIGGER);
        form.setAjax(ajax);

        Field field = new Field(FieldType.HIDDEN, "projectName");
        field.setValue(getProjectName());
        form.add(field);

        field = new Field(FieldType.TEXT, "revision");
        field.setLabel("revision");
        field.setValue(revision);
        addLatestAction(field, project, project.getConfig());

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
        newPanel = new ConfigurationPanel("aaction/edit-build-properties.vm");
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
        return PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, getProjectName());
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
        if (TextUtils.stringSet(revision))
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

            // CIB-1162: Make sure we can get a pulse file at this revision
            try
            {
                TypeConfiguration projectType = project.getConfig().getType();
                projectType.getPulseFile(project.getConfig(), r, null);
            }
            catch (Exception e)
            {
                addFieldError("revision", "Unable to get pulse file for revision: " + e.getMessage());
                LOG.severe(e);
                renderForm();
                return INPUT;
            }
        }

        try
        {
            projectManager.triggerBuild(project.getConfig(), mapProperties(project.getConfig()), new ManualTriggerBuildReason(getPrinciple()), r, ProjectManager.TRIGGER_CATEGORY_MANUAL, false, true);
        }
        catch (Exception e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }

        try
        {
            // Pause for dramatic effect
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // Empty
        }

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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
