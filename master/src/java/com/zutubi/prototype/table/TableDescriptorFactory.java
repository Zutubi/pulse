package com.zutubi.prototype.table;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.ConventionSupport;
import com.zutubi.util.logging.Logger;
import com.zutubi.config.annotations.Table;

import java.lang.reflect.Method;

/**
 * The table descriptor factory is an implementation of a descriptor factory that uses an objects type definition
 * to construct a table descriptor.
 *
 */
public class TableDescriptorFactory
{
    private static final Logger LOG = Logger.getLogger(TableDescriptorFactory.class);

    public TableDescriptor create(CompositeType type)
    {
        TableDescriptor td = new TableDescriptor();

        // default actions.
        td.addAction("edit");
        td.addAction("delete");

        Class handler = ConventionSupport.getActions(type);
        if (handler != null)
        {
            for (Method m : handler.getMethods())
            {
                if (!m.getName().startsWith("do"))
                {
                    continue;
                }
                if (m.getReturnType() != Void.TYPE)
                {
                    continue;
                }
                if (m.getParameterTypes().length != 1)
                {
                    continue;
                }
                Class param = m.getParameterTypes()[0];
                if (param != type.getClazz())
                {
                    continue;
                }
                // ok, we have an action here.
                td.addAction(m.getName().substring(2));
            }
        }

        Table tableAnnotation = (Table) type.getAnnotation(Table.class);
        if (tableAnnotation != null)
        {
            for (String columnName : tableAnnotation.columns())
            {
                ColumnDescriptor cd = new ColumnDescriptor(columnName);
                td.addColumn(cd);
            }
        }

        if (td.getColumns().size() == 0)
        {
            // By default, we extract all of the primitive properties and make them available to the table.
            // render properties as columns.
            for (String primitivePropertyName : type.getPropertyNames(PrimitiveType.class))
            {
                ColumnDescriptor cd = new ColumnDescriptor(primitivePropertyName);
                td.addColumn(cd);
            }
        }

        return td;
    }
}
