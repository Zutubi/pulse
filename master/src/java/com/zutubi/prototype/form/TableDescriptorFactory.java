package com.zutubi.prototype.form;

import com.zutubi.prototype.form.annotation.Table;
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
            // if there is a table annotation, use it to augment.
        }

        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(propertyInfo.getName());

        // generate columns...
        ColumnDescriptor columnDescriptor = new ColumnDescriptor();
        columnDescriptor.setFormatter(new SimpleColumnFormatter());
        columnDescriptor.setName(propertyInfo.getName() + ".header");
        tableDescriptor.addDescriptor(columnDescriptor);    

        //TODO: check that the user has the necessary Auth to view / execute these actions.

        // column b: actions (remove, edit)
        ColumnDescriptor actionColumnDescriptor = new ActionColumnDescriptor("edit", propertyInfo);
        tableDescriptor.addDescriptor(actionColumnDescriptor);

        actionColumnDescriptor = new ActionColumnDescriptor("delete", propertyInfo);
        tableDescriptor.addDescriptor(actionColumnDescriptor);

        //TODO: add the add action row to the base of this table.

        return tableDescriptor;
    }

    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
