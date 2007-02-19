package com.zutubi.prototype;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;

import java.beans.IntrospectionException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class TableDescriptorFactory
{
    private TypeRegistry typeRegistry;

    public List<TableDescriptor> createDescriptors(String symbolicName) throws IntrospectionException
    {
        Type type = typeRegistry.getType(symbolicName);
        if (!(type instanceof CompositeType))
        {
            throw new IllegalArgumentException("Can not create a table from a non-composite type: " + symbolicName);
        }
        CompositeType ctype = (CompositeType) type;
        return createDescriptors(ctype);
    }

    public TableDescriptor createDescriptor(String symbolicName, String propertyName)
    {
        Type type = typeRegistry.getType(symbolicName);
        if (!(type instanceof CompositeType))
        {
            throw new IllegalArgumentException("Can not retrieve a property from a non-composite type: " + symbolicName);
        }
        CompositeType ctype = (CompositeType) type;
        Type propertyType = ctype.getProperty(propertyName);
        return createTableDescriptor(propertyName, propertyType);
    }

    public List<TableDescriptor> createDescriptors(CompositeType type) throws IntrospectionException
    {
        List<TableDescriptor> tableDescriptors = new LinkedList<TableDescriptor>();

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (String name : type.getProperties(ListType.class))
        {
            tableDescriptors.add(createTableDescriptor(name, type.getProperty(name)));
        }
        return tableDescriptors;
    }

    private TableDescriptor createTableDescriptor(String name, Type propertyInfo)
    {
/*
        Annotation tableAnnotation = propertyInfo.getAnnotation(Table.class);
        if (tableAnnotation != null)
        {
            // if there is a table annotation, use it to augment the table description...
        }
*/

        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(name);

        // generate the header row.
        RowDescriptor headerRow = new RowDescriptor();
        headerRow.addDescriptor(new HeaderColumnDescriptor(name));
        headerRow.addDescriptor(new HeaderColumnDescriptor("action", 2));
        tableDescriptor.addDescriptor(headerRow);

        //TODO: check that the user has the necessary Auth to view / execute these actions.

        // generate data row.
        RowDescriptor dataRow = new ListRowDescriptor();
        dataRow.addDescriptor(new ColumnDescriptor());
        dataRow.addDescriptor(new ActionColumnDescriptor("edit"));
        dataRow.addDescriptor(new ActionColumnDescriptor("delete"));
        tableDescriptor.addDescriptor(dataRow);

        RowDescriptor addRowDescriptor = new RowDescriptor();
        addRowDescriptor.addDescriptor(new ActionColumnDescriptor("add", 3));
        tableDescriptor.addDescriptor(addRowDescriptor);

        return tableDescriptor;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
