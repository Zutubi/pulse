package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.web.ActionSupport;

import java.util.List;

/**
 *
 *
 */
public class SummaryAction extends ActionSupport
{
    private String path;

    private RecordManager recordManager;

    private TypeRegistry typeRegistry;
    private ConfigurationRegistry configurationRegistry;

    private List<String> primitiveTypes;
    private List<String> nestedTypes;
    private List<String> listTypes;
    private List<String> mapTypes;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public List<String> getPrimitiveTypes()
    {
        return primitiveTypes;
    }

    public List<String> getNestedTypes()
    {
        return nestedTypes;
    }

    public List<String> getListTypes()
    {
        return listTypes;
    }

    public List<String> getMapTypes()
    {
        return mapTypes;
    }

    public String execute() throws Exception
    {
        // convert path into symbolicName.
        String symbolicName = null;
        Record record = recordManager.load(path);
        if (record != null)
        {
            symbolicName = record.getMetaProperty("symbolicName");
        }

        if (symbolicName == null)
        {
            symbolicName = configurationRegistry.getSymbolicName(path);
        }

        CompositeType type = typeRegistry.getType(symbolicName);

        // convert type into something that can be rendered as a summary.

        listTypes = type.getProperties(ListType.class);
        mapTypes = type.getProperties(MapType.class);
        nestedTypes = type.getProperties(CompositeType.class);
        primitiveTypes = type.getProperties(PrimitiveType.class);

        return SUCCESS;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
