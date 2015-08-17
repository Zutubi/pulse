package com.zutubi.pulse.master.rest;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.model.tables.ColumnModel;
import com.zutubi.pulse.master.rest.model.tables.TableModel;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.PrimitiveType;

import java.util.Arrays;
import java.util.List;

/**
 * Builds {@link TableModel} instances out of annotated type information.
 *
 * FIXME kendo this replaces TableDescriptorFactory (TableModel replaces TableDescriptor, and Table
 *       dies)
 *
 * FIXME kendo it feels like this belongs in Tove (along with the models) see FormModelBuilder for
 *       details
 */
public class TableModelBuilder
{
    public TableModel createTable(CollectionType collectionType)
    {
        CompositeType targetType = (CompositeType) collectionType.getCollectionType();
        TableModel model = new TableModel(ToveUtils.getTableHeading(targetType));

        Table tableAnnotation = targetType.getAnnotation(Table.class, true);
        List<String> columnNames;
        if (tableAnnotation == null)
        {
            columnNames = targetType.getPropertyNames(PrimitiveType.class);
        }
        else
        {
            columnNames = Arrays.asList(tableAnnotation.columns());
        }

        addColumns(model, columnNames, targetType);

        return model;
    }

    private void addColumns(TableModel model, List<String> columnNames, CompositeType targetType)
    {
        Messages messages = Messages.getInstance(targetType.getClazz());

        for (String columnName: columnNames)
        {
            String key = columnName + ConventionSupport.I18N_KEY_SUFFIX_LABEL;
            model.addColumn(new ColumnModel(columnName, ToveUtils.format(messages, key)));
        }
    }

}
