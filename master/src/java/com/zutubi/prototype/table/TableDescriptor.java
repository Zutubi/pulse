package com.zutubi.prototype.table;

import com.zutubi.prototype.AbstractParameterised;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.i18n.Messages;
import com.zutubi.prototype.model.Cell;
import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.RowAction;
import com.zutubi.prototype.model.Table;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The table descriptor represents the model used to render a table to the UI.
 *
 *
 */
public class TableDescriptor extends AbstractParameterised
{
    private static final Logger LOG = Logger.getLogger(TableDescriptor.class);

    public static final String PARAM_HEADING = "heading";
    public static final String PARAM_ADD_ALLOWED = "addAllowed";

    private CompositeType type;
    /**
     * The columns descriptors associated with this table descriptor.
     */
    private List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();
    private ActionManager actionManager;

    public TableDescriptor(CompositeType type, boolean addAllowed, ActionManager actionManager)
    {
        this.actionManager = actionManager;
        this.type = type;
        addParameter(PARAM_HEADING, PrototypeUtils.getTableHeading(type));
        addParameter(PARAM_ADD_ALLOWED, addAllowed);
    }

    public Table instantiate(String path, Collection<Configuration> data)
    {
        int width = columns.size() + 1;
        Table table = new Table(width);

        Messages messages = Messages.getInstance(type.getClazz());

        for(ColumnDescriptor column: columns)
        {
            table.addHeader(messages.format(column.getName() + ".label"));
        }

        if(data != null && data.size() > 0)
        {
            for(Configuration instance: data)
            {
                Row row = new Row(instance.getConfigurationPath(), getActions(instance, messages));
                for(ColumnDescriptor column: columns)
                {
                    row.addCell(new Cell(column.getValue(instance).toString()));
                }
                table.addRow(row);
            }
        }
        else
        {
            Row nothingRow = new Row();
            nothingRow.addCell(new Cell(width, messages.format("no.data.available")));
            table.addRow(nothingRow);
        }

        table.addAll(parameters);
        return table;
    }

    private List<RowAction> getActions(Object instance, final Messages messages)
    {
        try
        {
            List<String> actionNames = actionManager.getActions((Configuration) instance, true);
            return CollectionUtils.map(actionNames, new Mapping<String, RowAction>()
            {
                public RowAction map(String actionName)
                {
                    return new RowAction(actionName, messages.format(actionName + ".label"));
                }
            });
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return Collections.EMPTY_LIST;
        }
    }

    public void addColumn(ColumnDescriptor descriptor)
    {
        columns.add(descriptor);
    }
}
