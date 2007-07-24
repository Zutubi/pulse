package com.zutubi.prototype.table;

import com.zutubi.config.annotations.Table;
import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.actions.Actions;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The table descriptor factory is an implementation of a descriptor factory that uses an objects type definition
 * to construct a table descriptor.
 *
 */
public class TableDescriptorFactory
{
    private ObjectFactory objectFactory;

    public TableDescriptor create(CompositeType type)
    {
        TableDescriptor td = new TableDescriptor(PrototypeUtils.getTableHeading(type));

        // default actions.
        ActionDescriptor ad = new ActionDescriptor();
        ad.addDefaultAction("edit");
        ad.addDefaultAction("delete");
        td.addActionDescriptor(ad);

        Class handler = ConventionSupport.getActions(type);
        if (handler != null)
        {
            Actions actions = new Actions();
            actions.setObjectFactory(objectFactory);
            ad.addActionHandler(handler, actions);
        }

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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
