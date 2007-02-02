package com.zutubi.prototype.form;

import com.zutubi.prototype.form.annotation.Table;
import com.zutubi.pulse.prototype.record.RecordPropertyInfo;
import com.zutubi.pulse.prototype.record.RecordTypeInfo;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.util.AnnotationUtils;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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
        List<TableDescriptor> tableDescriptors = new LinkedList<TableDescriptor>();

        BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);

        RecordTypeInfo typeInfo = typeRegistry.getInfo(type);

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
        {
            List<Annotation> annotations = AnnotationUtils.annotationsFromProperty(propertyDescriptor);
            Annotation tableAnnotation = CollectionUtils.find(annotations, new Predicate<Annotation>()
            {
                public boolean satisfied(Annotation annotation)
                {
                    return annotation instanceof Table;
                }
            });
            
            if (tableAnnotation != null)
            {
                tableDescriptors.add(createTableDescriptor(propertyDescriptor, tableAnnotation, typeInfo));
            }
        }
        return tableDescriptors;
    }

    public TableDescriptor createTableDescriptor(PropertyDescriptor propertyDescriptor, Annotation tableAnnotation, RecordTypeInfo typeInfo)
    {
        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(propertyDescriptor.getName());

        // generate columns...

        RecordPropertyInfo propertyInfo = typeInfo.getProperty(propertyDescriptor.getName());
        propertyInfo.getType();



        return tableDescriptor;
    }

    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
