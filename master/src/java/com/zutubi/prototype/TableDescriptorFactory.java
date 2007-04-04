package com.zutubi.prototype;

import com.zutubi.prototype.annotation.Format;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;

/**
 *
 *
 */
public class TableDescriptorFactory
{
    private TypeRegistry typeRegistry;

    public TableDescriptor createTableDescriptor(CollectionType type) throws TypeException
    {
        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(type.getSymbolicName());

        // generate the header row.
        RowDescriptor headerRow = new SingleRowDescriptor();
        headerRow.addDescriptor(new HeaderColumnDescriptor(((CompositeType)type.getCollectionType()).getSymbolicName()));
        headerRow.addDescriptor(new HeaderColumnDescriptor("action", 2));
        tableDescriptor.addDescriptor(headerRow);

        //TODO: check that the user has the necessary Auth to view / execute these actions.

        // generate data row.
        RowDescriptor dataRow = new CollectionRowDescriptor(type);

        // take a look at any annotations defined for the base collection type.
        Formatter defaultFormatter = new SimpleColumnFormatter();
        Type baseType = type.getCollectionType();
        Format format = (Format) baseType.getAnnotation(Format.class);
        if (format != null)
        {
            try
            {
                defaultFormatter = format.value().newInstance();
            }
            catch (Exception e)
            {
                throw new TypeException(e);
            }
        }

        ColumnDescriptor columnDescriptor = new SummaryColumnDescriptor(typeRegistry);
        columnDescriptor.setFormatter(new AnnotationFormatter(defaultFormatter));

        dataRow.addDescriptor(columnDescriptor);
        dataRow.addDescriptor(new ActionColumnDescriptor("edit"));
        dataRow.addDescriptor(new ActionColumnDescriptor("delete"));
        tableDescriptor.addDescriptor(dataRow);

        RowDescriptor addRowDescriptor = new SingleRowDescriptor();
        addRowDescriptor.addDescriptor(new ActionColumnDescriptor("add", 3));
        tableDescriptor.addDescriptor(addRowDescriptor);

        return tableDescriptor;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
