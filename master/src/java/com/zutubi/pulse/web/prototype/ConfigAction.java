package com.zutubi.pulse.web.prototype;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.ConfigurationDescriptor;
import com.zutubi.prototype.ConfigurationDescriptorFactory;
import com.zutubi.prototype.Path;
import com.zutubi.prototype.model.Config;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.prototype.record.RecordManager;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.web.ActionSupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class ConfigAction extends ActionSupport
{
    private Path path;

    private ProjectConfigurationManager projectConfigurationManager;
    private RecordTypeRegistry recordTypeRegistry;

    private RecordManager recordManager;

    private Config config;

    private String submit;

    private Messages messages;

    public void setSubmit(String submit)
    {
        this.submit = submit;
    }

    public List<String> getPathElements()
    {
        List<String> elements = new LinkedList<String>();
        elements.addAll(path.getPathElements());
        return elements;
    }

    public String getPath()
    {
        return path.toString();
    }

    public void setPath(String path)
    {
        this.path = new Path(path);
    }

    public String getParentPath()
    {
        return this.path.getParent().toString();
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
        // if we have a record, then display the page for it. Otherwise, it is a new page.
        Record record = recordManager.load(path.toString());
        if (record != null)
        {
            String symbolicName = record.getSymbolicName();
            prepareConfigDescriptor(symbolicName, record);
        }
        else
        {
            String symbolicName = projectConfigurationManager.getSymbolicName(path);
            prepareConfigDescriptor(symbolicName, record);
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
        hiddenFields.add("path");

        // Extract the submit data.
        Map<String, String> data = new HashMap<String, String>();
        for (String key : parameters.keySet())
        {
            if (!hiddenFields.contains(key))
            {
                data.put(key, parameters.get(key)[0]);
            }
        }

        // extract the record and update - need to create a new record if one does not exist...
        Record record = recordManager.load(path.toString());
        record.putAll(data);

        String symbolicName = record.getSymbolicName();

        // we can only show config pages for objects that have symbolicNames...
        prepareConfigDescriptor(symbolicName, record);

        return SUCCESS;
    }

    private void prepareConfigDescriptor(String symbolicName, Record record)
    {
        // Setup the config object so that we can render the page.
        ConfigurationDescriptorFactory configurationDescriptorFactory = new ConfigurationDescriptorFactory();
        configurationDescriptorFactory.setRecordTypeRegistry(recordTypeRegistry);
        ConfigurationDescriptor configDescriptor = configurationDescriptorFactory.createDescriptor(symbolicName);
        config = configDescriptor.instantiate(record);

        messages = Messages.getInstance(recordTypeRegistry.getType(symbolicName));
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager projectConfigurationManager)
    {
        this.projectConfigurationManager = projectConfigurationManager;
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
