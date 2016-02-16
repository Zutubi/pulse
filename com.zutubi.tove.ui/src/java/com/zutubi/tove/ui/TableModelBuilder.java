package com.zutubi.tove.ui;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.PrimitiveType;
import com.zutubi.tove.ui.model.tables.ColumnModel;
import com.zutubi.tove.ui.model.tables.TableModel;

import java.util.Arrays;
import java.util.List;

/**
 * Builds {@link TableModel} instances out of annotated type information.
 *
 * FIXME kendo it feels like this belongs in Tove (along with the models) see FormModelBuilder for
 *       details
 */
public class TableModelBuilder
{
    private static final String KEY_TABLE_HEADING = "table.heading";

    public TableModel createTable(CollectionType collectionType)
    {
        CompositeType targetType = (CompositeType) collectionType.getCollectionType();
        TableModel model = new TableModel(getTableHeading(targetType));

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

    private String getTableHeading(CompositeType type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        if(messages.isKeyDefined(KEY_TABLE_HEADING))
        {
            return messages.format(KEY_TABLE_HEADING);
        }
        else
        {
            return ToveUiUtils.getPluralLabel(messages);
        }
    }


    private void addColumns(TableModel model, List<String> columnNames, CompositeType targetType)
    {
        Messages messages = Messages.getInstance(targetType.getClazz());

        for (String columnName: columnNames)
        {
            String key = columnName + ConventionSupport.I18N_KEY_SUFFIX_LABEL;
            model.addColumn(new ColumnModel(columnName, ToveUiUtils.format(messages, key)));
        }
    }

}
