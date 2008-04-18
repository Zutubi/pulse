package com.zutubi.pulse.web.project;

import com.opensymphony.xwork.ActionContext;
import static com.zutubi.config.annotations.FieldParameter.ACTIONS;
import static com.zutubi.config.annotations.FieldParameter.SCRIPTS;
import com.zutubi.config.annotations.FieldType;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.config.NamedConfigurationComparator;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class EditBuildPropertiesAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(EditBuildPropertiesAction.class);

    private static final String PROPERTY_PREFIX = "property.";

    private String formSource;
    private String revision;
    private List<ResourceProperty> properties;

    private ScmClientFactory<ScmConfiguration> scmClientFactory;
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;

    public String getFormSource()
    {
        return formSource;
    }

    public List<ResourceProperty> getProperties()
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

    private void renderForm() throws IOException, TemplateException
    {
        Project project = getRequiredProject();
        properties = new ArrayList<ResourceProperty>(project.getConfig().getProperties().values());
        Collections.sort(properties, new NamedConfigurationComparator());

        Form form = new Form("form", "edit.build.properties", "editBuildProperties.action");
        form.setAjax(false);

        Field field = new Field(FieldType.HIDDEN, "projectName");
        field.setValue(getProjectName());
        form.add(field);

        field = new Field(FieldType.TEXT, "revision");
        field.setLabel("revision");
        field.setValue(revision);
        field.addParameter(ACTIONS, Arrays.asList("getlatest"));
        field.addParameter(SCRIPTS, Arrays.asList("EditBuildPropertiesAction.getlatest"));
        form.add(field);

        for(ResourceProperty property: properties)
        {
            field = new Field(FieldType.TEXT, PROPERTY_PREFIX + property.getName());
            field.setLabel(property.getName());
            field.setValue(property.getValue());
            form.add(field);
        }

        addSubmit(form, "trigger");
        addSubmit(form, "cancel");

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("projectId", project.getId());

        StringWriter writer = new StringWriter();
        PrototypeUtils.renderForm(context, form, getClass(), writer, configurationManager);
        formSource = writer.toString();
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

    public String execute() throws IOException, TemplateException
    {
        Project project = getRequiredProject();
        getProjectManager().checkWrite(project);

        ProjectConfiguration projectConfig = configurationProvider.deepClone(project.getConfig());
        mapProperties(projectConfig);
        String path = configurationProvider.save(projectConfig);
        projectConfig = configurationProvider.get(path, ProjectConfiguration.class);

        Revision r = null;
        if(TextUtils.stringSet(revision))
        {
            ScmClient client = null;
            try
            {
                client = scmClientFactory.createClient(projectConfig.getScm());
                r = client.getRevision(revision);
            }
            catch (ScmException e)
            {
                addFieldError("revision", "Unable to verify revision: " + e.getMessage());
                LOG.severe(e);
                renderForm();
                return INPUT;
            }
            finally
            {
                ScmClientUtils.close(client);
            }
            
            // CIB-1162: Make sure we can get a pulse file at this revision
            try
            {
                TypeConfiguration projectType = projectConfig.getType();
                ComponentContext.autowire(projectType);
                projectType.getPulseFile(0L, projectConfig, r, null);
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
            projectManager.triggerBuild(projectConfig, new ManualTriggerBuildReason((String)getPrinciple()), r, true);
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

        return SUCCESS;
    }

    private void mapProperties(ProjectConfiguration projectConfig)
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(PROPERTY_PREFIX))
            {
                String propertyName = name.substring(PROPERTY_PREFIX.length());
                ResourceProperty property = projectConfig.getProperty(propertyName);
                if(property != null)
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
        }
    }

    public void setScmClientFactory(ScmClientFactory<ScmConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
