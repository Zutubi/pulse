package com.zutubi.prototype;

import com.zutubi.prototype.annotation.Table;
import com.zutubi.pulse.prototype.record.RecordTypeInfo;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.prototype.record.ValueListRecordPropertyInfo;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class TableDescriptorFactory
{
    private RecordTypeRegistry typeRegistry;

    public List<TableDescriptor> createDescriptors(Class type) throws IntrospectionException
    {
        RecordTypeInfo typeInfo = typeRegistry.getInfo(type);
        return createDescriptors(typeInfo);
    }

    public List<TableDescriptor> createDescriptors(String symbolicName) throws IntrospectionException
    {
        RecordTypeInfo typeInfo = typeRegistry.getInfo(symbolicName);
        return createDescriptors(typeInfo);
    }

    public List<TableDescriptor> createDescriptors(RecordTypeInfo typeInfo) throws IntrospectionException
    {
        List<TableDescriptor> tableDescriptors = new LinkedList<TableDescriptor>();

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (ValueListRecordPropertyInfo propertyInfo : typeInfo.getValueListInfos())
        {
            tableDescriptors.add(createTableDescriptor(propertyInfo));
        }
        return tableDescriptors;
    }

    private TableDescriptor createTableDescriptor(ValueListRecordPropertyInfo propertyInfo)
    {
        Annotation tableAnnotation = propertyInfo.getAnnotation(Table.class);
        if (tableAnnotation != null)
        {
            // if there is a table annotation, use it to augment the table description...
        }

        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(propertyInfo.getName());

        // generate the header row.
        RowDescriptor headerRow = new RowDescriptor();
        headerRow.addDescriptor(new HeaderColumnDescriptor(propertyInfo.getName()));
        headerRow.addDescriptor(new HeaderColumnDescriptor("action", 2));
        tableDescriptor.addDescriptor(headerRow);

        //TODO: check that the user has the necessary Auth to view / execute these actions.

        // generate data row.
        RowDescriptor dataRow = new ValueListRowDescriptor();
        dataRow.addDescriptor(new ColumnDescriptor());
        dataRow.addDescriptor(new ActionColumnDescriptor("edit", propertyInfo));
        dataRow.addDescriptor(new ActionColumnDescriptor("delete", propertyInfo));
        tableDescriptor.addDescriptor(dataRow);

        RowDescriptor addRowDescriptor = new RowDescriptor();
        addRowDescriptor.addDescriptor(new ActionColumnDescriptor("add", propertyInfo, 3));
        tableDescriptor.addDescriptor(addRowDescriptor);

        return tableDescriptor;
    }

    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
