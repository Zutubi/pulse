package com.zutubi.prototype.table;

import com.zutubi.config.annotations.Table;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.webwork.PrototypeUtils;

/**
 * The table descriptor factory is an implementation of a descriptor factory that uses an objects type definition
 * to construct a table descriptor.
 *
 */
public class TableDescriptorFactory
{
    private ActionManager actionManager;
    private ConfigurationSecurityManager configurationSecurityManager;

    public TableDescriptor create(String path, CompositeType type)
    {
        TableDescriptor td = new TableDescriptor(PrototypeUtils.getTableHeading(type), configurationSecurityManager.hasPermission(path, AccessManager.ACTION_CREATE));

        // default actions.
        ActionDescriptor ad = new ActionDescriptor(actionManager);
        td.addActionDescriptor(ad);

        // does the table has a Table annotation defining the columns to be rendered?
        Table tableAnnotation = (Table) type.getAnnotation(Table.class);
        if (tableAnnotation != null)
        {
            for (String columnName : tableAnnotation.columns())
            {
                ColumnDescriptor cd = new ColumnDescriptor(columnName);
                cd.setType(type);
                td.addColumn(cd);
            }
        }

        // default table definition is based on the types primitive properties.
        if (td.getColumns().size() == 0)
        {
            // By default, we extract all of the primitive properties and make them available to the table.
            // render properties as columns.
            for (String primitivePropertyName : type.getPropertyNames(PrimitiveType.class))
            {
                ColumnDescriptor cd = new ColumnDescriptor(primitivePropertyName);
                cd.setType(type);
                td.addColumn(cd);
            }
        }

        return td;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }
}
