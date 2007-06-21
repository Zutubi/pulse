package com.zutubi.prototype.table;

import com.zutubi.config.annotations.Table;
import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.actions.Actions;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * The table descriptor factory is an implementation of a descriptor factory that uses an objects type definition
 * to construct a table descriptor.
 *
 */
public class TableDescriptorFactory
{
    private static final Logger LOG = Logger.getLogger(TableDescriptorFactory.class);

    private ObjectFactory objectFactory;

    public TableDescriptor create(CompositeType type)
    {
        TableDescriptor td = new TableDescriptor();

        // default actions.
        td.addAction("edit");
        td.addAction("delete");

        Class handler = ConventionSupport.getActions(type);

        //FIXME: convert this to using the Actions object.  Problem: actions object requires an instance of the
        //       object being represented, so that if necessary, the getActions(instance) method can be supported.
        //       We do not have that instance at this stage.
        if (handler != null)
        {
            Actions actions = new Actions();
            actions.setObjectFactory(objectFactory);

            List<String> defaultActions = actions.getDefaultActions(handler, type.getClazz());
            
            for (String actionName : defaultActions)
            {
                // ok, we have an action here.
                td.addAction(actionName);
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
