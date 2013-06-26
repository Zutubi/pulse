package com.zutubi.pulse.master.tove.table;

import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.PrimitiveType;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The table descriptor factory is an implementation of a descriptor factory that uses an objects type definition
 * to construct a table descriptor.
 *
 */
public class TableDescriptorFactory
{
    private ActionManager actionManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationProvider configurationProvider;
    private ObjectFactory objectFactory;
    private SystemPaths systemPaths;

    public TableDescriptor create(String path, CollectionType collectionType)
    {
        CompositeType type = (CompositeType) collectionType.getCollectionType();
        TableDescriptor td = new TableDescriptor(collectionType, configurationSecurityManager.hasPermission(path, AccessManager.ACTION_WRITE), configurationSecurityManager.hasPermission(path, AccessManager.ACTION_CREATE), configurationProvider, actionManager, systemPaths);

        // does the table has a Table annotation defining the columns to be rendered?
        Table tableAnnotation = type.getAnnotation(Table.class, true);
        if (tableAnnotation != null)
        {
            for (String columnName : tableAnnotation.columns())
            {
                td.addColumn(newColumnDescriptor(columnName, type));
            }
        }
        else
        {
            // By default, we extract all of the primitive properties and make them available.
            for (String primitivePropertyName : type.getPropertyNames(PrimitiveType.class))
            {
                td.addColumn(newColumnDescriptor(primitivePropertyName, type));
            }
        }

        return td;
    }

    private ColumnDescriptor newColumnDescriptor(String name, CompositeType type)
    {
        return objectFactory.buildBean(ColumnDescriptor.class, name, type);
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
