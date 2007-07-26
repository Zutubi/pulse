package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.velocity.PrototypeDirective;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.config.NamedConfigurationComparator;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EditBuildPropertiesAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(EditBuildPropertiesAction.class);

    private static final String PROPERTY_PREFIX = "property.";

    private String formSource;
    private String revision;
    private List<ResourceProperty> properties;
    private Configuration freemarkerConfiguration;

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
        properties = new ArrayList<ResourceProperty>(getProjectConfig().getProperties().values());
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

        StringWriter writer = new StringWriter();
        Template template = freemarkerConfiguration.getTemplate("prototype/xhtml/form.ftl");
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
        getProjectManager().checkWrite(getProject());

        mapProperties();
        // FIXME the values here do not seem to persist, but we are saving
        // FIXME something accurate...also saving at this granularity is very
        // FIXME heavy handed

        projectManager.saveProjectConfig(getProjectConfig());

        Revision r = null;
        if(TextUtils.stringSet(revision))
        {
            try
            {
                r = getProjectConfig().getScm().createClient().getRevision(revision);
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
                TypeConfiguration projectType = getProjectConfig().getType();
                ComponentContext.autowire(projectType);
                projectType.getPulseFile(0L, getProjectConfig(), r, null);
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
            projectManager.triggerBuild(getProjectConfig(), new ManualTriggerBuildReason((String)getPrinciple()), r, true);
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

    private void mapProperties()
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(PROPERTY_PREFIX))
            {
                String propertyName = name.substring(PROPERTY_PREFIX.length());
                ResourceProperty property = getProjectConfig().getProperty(propertyName);
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

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
}
