package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.velocity.PrototypeDirective;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
import com.zutubi.pulse.core.config.NamedConfigurationComparator;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.util.logging.Logger;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
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

    private ScmClientFactory scmClientFactory;
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

        Form form = new Form();
        form.setAjax(false);
        form.setName("form");
        form.setId("edit.build.properties");
        form.setAction("editBuildProperties.action");

        Field field = new Field();
        field.setType("hidden");
        field.setId("zfid.projectName");
        field.setName("projectName");
        field.setValue(getProjectName());
        form.add(field);

        field = new Field();
        field.setType("text");
        field.setId("zfid.revision");
        field.setName("revision");
        field.setLabel("revision");
        field.setValue(revision);
        field.addParameter("actions", Arrays.asList("getlatest"));
        field.addParameter("scripts", Arrays.asList("EditBuildPropertiesAction.getlatest"));
        form.add(field);

        for(ResourceProperty property: properties)
        {
            field = new Field();
            field.setType("text");
            field.setId("zfid." + PROPERTY_PREFIX + property.getName());
            field.setName(PROPERTY_PREFIX + property.getName());
            field.setLabel(property.getName());
            field.setValue(property.getValue());
            form.add(field);
        }

        addSubmit(form, "trigger");
        addSubmit(form, "cancel");

        Map<String, Object> context = PrototypeDirective.initialiseContext(getClass());
        context.put("form", form);
        context.put("actionErrors", getActionErrors());
        context.put("fieldErrors", getFieldErrors());

        Configuration configuration = FreemarkerConfigurationFactoryBean.createConfiguration(configurationManager);
        configuration.setSharedVariable("projectId", project.getId());
        TemplateLoader currentLoader = configuration.getTemplateLoader();
        TemplateLoader classLoader = new ClassTemplateLoader(getClass(), "");
        MultiTemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{ classLoader, currentLoader });
        configuration.setTemplateLoader(loader);

        StringWriter writer = new StringWriter();
        Template template = configuration.getTemplate("prototype/xhtml/form.ftl");
        template.process(context, writer);

        formSource = writer.toString();
    }

    private void addSubmit(Form form, String name)
    {
        Field field;
        field = new Field();
        field.setType("submit");
        field.setName(name);
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
            try
            {
                ScmClient client = scmClientFactory.createClient(projectConfig.getScm());
                r = client.getRevision(revision);
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
                TypeConfiguration projectType = projectConfig.getType();
                ComponentContext.autowire(projectType);
                projectType.getPulseFile(0L, projectConfig, r, null);
            }
            catch (Exception e)
            {
                addFieldError("revision", "Unable to get pulse file for revision: " + e.getMessage());
                LOG.severe(e);
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

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
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
