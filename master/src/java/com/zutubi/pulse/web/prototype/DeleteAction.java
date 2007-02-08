package com.zutubi.pulse.web.prototype;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.pulse.prototype.TemplateRecord;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.prototype.model.Config;
import com.zutubi.prototype.ConfigurationDescriptorFactory;
import com.zutubi.prototype.ConfigurationDescriptor;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

import java.util.*;

/**
 *
 *
 */
public class DeleteAction extends ActionSupport
{
    private String scope;
    private String path;

    private ProjectConfigurationManager projectConfigurationManager;
    private RecordTypeRegistry recordTypeRegistry;

    private Config config;

    private String submit;

    private Messages messages;

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

    public Config getConfig()
    {
        return config;
    }

    public String getText(String aTextName)
    {
        return messages.format(aTextName);
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

            ConfigurationDescriptorFactory configurationDescriptorFactory = new ConfigurationDescriptorFactory();
            configurationDescriptorFactory.setRecordTypeRegistry(recordTypeRegistry);
            ConfigurationDescriptor configDescriptor = configurationDescriptorFactory.createDescriptor(symbolicName);
            config = configDescriptor.instantiate(record);

            messages = Messages.getInstance(recordTypeRegistry.getType(symbolicName));
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

        String symbolicName = projectConfigurationManager.getSymbolicName(path);
        TemplateRecord record = projectConfigurationManager.getRecord(projectId, path);

        ConfigurationDescriptorFactory configurationDescriptorFactory = new ConfigurationDescriptorFactory();
        configurationDescriptorFactory.setRecordTypeRegistry(recordTypeRegistry);
        ConfigurationDescriptor configDescriptor = configurationDescriptorFactory.createDescriptor(symbolicName);
        config = configDescriptor.instantiate(record);

        messages = Messages.getInstance(recordTypeRegistry.getType(symbolicName));

        return SUCCESS;
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager projectConfigurationManager)
    {
        this.projectConfigurationManager = projectConfigurationManager;
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}
