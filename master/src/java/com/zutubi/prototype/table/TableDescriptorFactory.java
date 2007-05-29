package com.zutubi.prototype.table;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.logging.Logger;

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
        td.add("edit");
        td.add("delete");


        Object info = getInfo(type);
        if (info != null)
        {
            if (info instanceof TableDefinition)
            {
                TableDefinition def = (TableDefinition) info;
                for (String columnName : def.getColumns())
                {
/*
                    TypeProperty property = type.getProperty(columnName);
                    if (property == null)
                    {
                        continue;
                    }
*/

                    ColumnDescriptor cd = new ColumnDescriptor();
                    cd.setName(columnName);
                    td.add(cd);
                }
            }
            if (info instanceof ActionDefinition)
            {
                ActionDefinition def = (ActionDefinition) info;
                // inspect and extract the doXXX action defs.

                Class handler = def.getActionHandler();
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
                    td.add(m.getName().substring(2));
                }
            }
        }

        if (td.getColumns().size() == 0)
        {
            // By default, we extract all of the primitive properties and make them available to the table.
            // render properties as columns.
            for (String primitivePropertyName : type.getPropertyNames(PrimitiveType.class))
            {
                ColumnDescriptor cd = new ColumnDescriptor();
                cd.setName(primitivePropertyName);
                td.add(cd);
            }
        }

        return td;
    }

    private Object getInfo(CompositeType type)
    {
        Class clazz = type.getClazz();
        try
        {
            String infoClassName = clazz.getCanonicalName() + "Info";
            Class infoClazz = clazz.getClassLoader().loadClass(infoClassName);
            return infoClazz.newInstance();
        }
        catch (Exception e)
        {
            LOG.debug(e);
        }
        return null;
    }
}
