package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.prototype.Path;
import com.zutubi.prototype.ConfigurationDescriptorFactory;
import com.zutubi.prototype.ConfigurationDescriptor;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.model.Config;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/**
 *
 *
 */
public class CrudAction extends ActionSupport
{
    private Path path;

    private ConfigurationRegistry configRegistry;
    private TypeRegistry typeRegistry;
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
            if (submit.equals("delete"))
            {
                return doDelete();
            }
        }
        return doDefault();
    }

    public String doDefault() throws Exception
    {
        // if we have a record, then display the page for it. Otherwise, it is a new page.
        String symbolicName = null;
        Record record = recordManager.load(path.toString());
        if (record != null)
        {
            symbolicName = record.getMetaProperty("symbolicName");
        }
        if (symbolicName == null)
        {
            symbolicName = configRegistry.getSymbolicName(path.toString());
        }
        prepareConfigDescriptor(symbolicName, record);

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
        hiddenFields.add("submit");

        // Extract the submit data.
        Map<String, Object> data = new HashMap<String, Object>();
        for (String key : parameters.keySet())
        {
            if (!hiddenFields.contains(key))
            {
                data.put(key, parameters.get(key)[0]);
            }
        }

        // extract the record and update - need to create a new record if one does not exist...
        String symbolicName = configRegistry.getSymbolicName(path.toString());
        Record record = recordManager.load(path.toString());
        if (record != null)
        {
            record.putAll(data);
        }
        else
        {
            record = new Record();
            record.putMetaProperty("symbolicName", symbolicName);
            record.putAll(data);
            recordManager.store(path.toString(), record);
        }

        // we can only show config pages for objects that have symbolicNames...
        prepareConfigDescriptor(symbolicName, record);

        return SUCCESS;
    }

    private String doDelete()
    {
        Object obj = recordManager.delete(path.toString());

        // for now, update the path until we get to a registered configuration. Later, this will not be a problem
        // since it will be an ajax callback.
        path = path.getParent();
        while (configRegistry.getSymbolicName(path.toString()) == null)
        {
            path = path.getParent();
        }

        return SUCCESS;
    }

    private void prepareConfigDescriptor(String symbolicName, Record record)
    {
        // Setup the config object so that we can render the page.
        ConfigurationDescriptorFactory configurationDescriptorFactory = new ConfigurationDescriptorFactory();
        configurationDescriptorFactory.setTypeRegistry(typeRegistry);
        ConfigurationDescriptor configDescriptor = configurationDescriptorFactory.createDescriptor(symbolicName);
        config = configDescriptor.instantiate(record);

        messages = Messages.getInstance(typeRegistry.getType(symbolicName));
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configRegistry)
    {
        this.configRegistry = configRegistry;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
