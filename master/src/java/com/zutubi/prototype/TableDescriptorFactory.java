package com.zutubi.prototype;

import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;

/**
 *
 *
 */
public class TableDescriptorFactory
{
    private TypeRegistry typeRegistry;

    public TableDescriptor createDescriptor(String symbolicName)
    {
        Type type = typeRegistry.getType(symbolicName);
        if (type instanceof CollectionType)
        {
            return createTableDescriptor((CollectionType) type);
        }
        return null;
    }

    public TableDescriptor createTableDescriptor(CollectionType type)
    {
/*
        Annotation tableAnnotation = propertyInfo.getAnnotation(Table.class);
        if (tableAnnotation != null)
        {
            // if there is a table annotation, use it to augment the table description...
        }
*/

        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(type.getSymbolicName());

        // generate the header row.
        RowDescriptor headerRow = new RowDescriptor();
        headerRow.addDescriptor(new HeaderColumnDescriptor(type.getSymbolicName()));
        headerRow.addDescriptor(new HeaderColumnDescriptor("action", 2));
        tableDescriptor.addDescriptor(headerRow);

        //TODO: check that the user has the necessary Auth to view / execute these actions.

        // generate data row.
        RowDescriptor dataRow = null;
        if (type instanceof MapType)
        {
            dataRow = new MapRowDescriptor();
        }
        else if (type instanceof ListType)
        {
            dataRow = new ListRowDescriptor();
        }
        
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
