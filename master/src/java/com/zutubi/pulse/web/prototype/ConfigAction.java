package com.zutubi.pulse.web.prototype;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.pulse.prototype.TemplateRecord;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.web.ActionSupport;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.*;

/**
 *
 *
 */
public class ConfigAction extends ActionSupport
{
    private String scope;
    private String path;

    private ProjectConfigurationManager projectConfigurationManager;
    private RecordTypeRegistry recordTypeRegistry;
    private Configuration configuration;

    private String formHtml;

    private String submit;

    public void setSubmit(String submit)
    {
        this.submit = submit;
    }

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public List<String> getPathElements()
    {
        List<String> elements = new LinkedList<String>();
        elements.addAll(Arrays.asList(path.split("/")));
        return elements;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getFormHtml()
    {
        return formHtml;
    }

    public String execute() throws Exception
    {
        if (TextUtils.stringSet(submit))
        {
            if (submit.equals("save"))
            {
                return doSave();
            }
            if (submit.equals("cancel"))
            {
                return doCancel();
            }
        }
        return doDefault();
    }

    public String doDefault() throws Exception
    {
        // extract the scope details.
        if (scope.startsWith("project"))
        {
            long projectId = Long.valueOf(scope.substring(8));

            String symbolicName = projectConfigurationManager.getSymbolicName(path);
            TemplateRecord record = projectConfigurationManager.getRecord(projectId, path);

            // render this record.
            formHtml = render(symbolicName, record);

            recordTypeRegistry.getInfo(symbolicName);
        }

        return SUCCESS;
    }

    public String doCancel() throws Exception
    {
        return doDefault();
    }

    public String doSave() throws Exception
    {
        // save the changes.
        Map<String, String[]> parameters = ActionContext.getContext().getParameters();

        Set<String> hiddenFields = new HashSet<String>();
        hiddenFields.add("scope");
        hiddenFields.add("path");

        Map<String, String> data = new HashMap<String, String>();
        for (String key : parameters.keySet())
        {
            if (!hiddenFields.contains(key))
            {
                data.put(key, parameters.get(key)[0]);
            }
        }

        // extract project id from scope.
        long projectId = Long.valueOf(scope.substring(8));
        projectConfigurationManager.setRecord(projectId, getPath(), data);

        // regenerate the form html.
        String symbolicName = projectConfigurationManager.getSymbolicName(path);
        TemplateRecord record = projectConfigurationManager.getRecord(projectId, path);

        formHtml = render(symbolicName, record);

        return SUCCESS;
    }

    private String render(String symbolicName, Map data) throws Exception
    {
        FormDescriptorFactory formFactory = new FormDescriptorFactory();
        formFactory.setTypeRegistry(recordTypeRegistry);
        FormDescriptor formDescriptor = formFactory.createDescriptor(symbolicName);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("form", formDescriptor.instantiate(data));
        context.put("i18nText", new GetTextMethod());
        context.put("path", getPath());
        context.put("scope", getScope());

        // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
        DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        Template template = configuration.getTemplate("form.ftl");
        template.process(context, writer);

        return writer.toString();
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager projectConfigurationManager)
    {
        this.projectConfigurationManager = projectConfigurationManager;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}
