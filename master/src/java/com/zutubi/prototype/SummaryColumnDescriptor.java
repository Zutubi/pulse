package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 *
 *
 */
public class SummaryColumnDescriptor extends ColumnDescriptor
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public SummaryColumnDescriptor(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public Column instantiate(String path, Record record)
    {
        Object instance = configurationPersistenceManager.getInstance(path);
        if(instance == null)
        {
            return null;
        }

        Column c = new Column();
        c.setSpan(colspan);
        c.addAll(getParameters());
        c.setValue(formatter.format(instance));
        return c;
    }
}
