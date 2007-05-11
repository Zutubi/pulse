package com.zutubi.prototype;

import com.zutubi.config.annotations.Format;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.util.ClassLoaderUtils;

/**
 *
 *
 */
public class TableDescriptorFactory
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public TableDescriptor createTableDescriptor(CollectionType type, boolean ajax) throws TypeException
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
        Formatter<Object> defaultFormatter = new SimpleColumnFormatter();
        CompositeType baseType = (CompositeType) type.getCollectionType();
        Format format = (Format) baseType.getAnnotation(Format.class);
        if (format != null)
        {
            try
            {
                // FIXME inefficient, just like formatting in the annotation
                // FIXME formatter itself.
                Class formatterClass = ClassLoaderUtils.loadAssociatedClass(baseType.getClazz(), format.value());
                defaultFormatter = (Formatter) formatterClass.newInstance();
            }
            catch (Exception e)
            {
                throw new TypeException(e);
            }
        }

        ColumnDescriptor columnDescriptor = new SummaryColumnDescriptor(configurationPersistenceManager);
        columnDescriptor.setFormatter(new AnnotationFormatter(defaultFormatter));

        dataRow.addDescriptor(columnDescriptor);
        dataRow.addDescriptor(new EditColumnDescriptor(ajax));
        dataRow.addDescriptor(new DeleteColumnDescriptor(ajax));
        tableDescriptor.addDescriptor(dataRow);

        RowDescriptor addRowDescriptor = new SingleRowDescriptor();
        addRowDescriptor.addDescriptor(new AddColumnDescriptor(3, ajax));
        tableDescriptor.addDescriptor(addRowDescriptor);

        return tableDescriptor;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
