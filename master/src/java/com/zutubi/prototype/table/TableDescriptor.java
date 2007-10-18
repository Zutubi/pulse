package com.zutubi.prototype.table;

import com.zutubi.prototype.AbstractParameterised;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.i18n.Messages;
import com.zutubi.prototype.model.Cell;
import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.RowAction;
import com.zutubi.prototype.model.Table;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The table descriptor represents the model used to render a table to the UI.
 *
 *
 */
public class TableDescriptor extends AbstractParameterised implements Descriptor
{
    private static final Logger LOG = Logger.getLogger(TableDescriptor.class);

    public static final String PARAM_HEADING = "heading";
    public static final String PARAM_ADD_ALLOWED = "addAllowed";

    private CollectionType collectionType;
    private CompositeType type;
    /**
     * The columns descriptors associated with this table descriptor.
     */
    private List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();
    private ConfigurationTemplateManager configurationTemplateManager;
    private ActionManager actionManager;

    public TableDescriptor(CollectionType collectionType, boolean addAllowed, ConfigurationTemplateManager configurationTemplateManager, ActionManager actionManager)
    {
        this.collectionType = collectionType;
        this.type = (CompositeType) collectionType.getCollectionType();

        addParameter(PARAM_HEADING, PrototypeUtils.getTableHeading(type));
        addParameter(PARAM_ADD_ALLOWED, addAllowed);

        this.configurationTemplateManager = configurationTemplateManager;
        this.actionManager = actionManager;
    }

    public Table instantiate(String path, Record data)
    {
        int width = columns.size() + 2;
        Table table = new Table(width);

        Messages messages = Messages.getInstance(type.getClazz());

        for(ColumnDescriptor column: columns)
        {
            table.addHeader(messages.format(column.getName() + ".label"));
        }

        if(data != null)
        {
            for(String key: collectionType.getOrder(data))
            {
                String itemPath = PathUtils.getPath(path, key);
                Configuration instance = configurationTemplateManager.getInstance(itemPath);
                Row row = new Row(itemPath, getActions(instance, messages));
                addCells(row, instance);
                applyDecorations(data, key, row, messages);
                table.addRow(row);
            }

            if(data instanceof TemplateRecord)
            {
                TemplateRecord templateRecord = (TemplateRecord) data;
                TemplateRecord templateParent = templateRecord.getParent();
                if(templateParent != null)
                {
                    String parentId = templateParent.getOwner();
                    String[] elements = PathUtils.getPathElements(path);
                    String parentPath = PathUtils.getPath(elements[0], parentId, PathUtils.getPath(2, elements));

                    List<String> hiddenKeys = new LinkedList<String>(templateRecord.getHiddenKeys());
                    Collections.sort(hiddenKeys, collectionType.getKeyComparator());
                    for(String hidden: hiddenKeys)
                    {
                        String parentItemPath = PathUtils.getPath(parentPath, hidden);
                        Configuration instance = configurationTemplateManager.getInstance(parentItemPath);
                        Row row = new Row(PathUtils.getPath(path, hidden), Arrays.asList(new RowAction("restore", messages.format("restore.label"))));
                        addCells(row, instance);
                        row.addParameter("hiddenFrom", templateParent.getOwner(hidden));
                        row.addParameter("cls", "item-hidden");
                        table.addRow(row);
                    }
                }
            }
        }

        if(table.getRows().size() == 0)
        {
            Row nothingRow = new Row();
            nothingRow.addCell(new Cell(width, messages.format("no.data.available")));
            table.addRow(nothingRow);
        }

        table.addAll(parameters);
        return table;
    }

    private void addCells(Row row, Configuration instance)
    {
        for(ColumnDescriptor column: columns)
        {
            row.addCell(new Cell(column.getValue(instance).toString()));
        }
    }

    private void applyDecorations(Record data, String key, Row row, Messages messages)
    {
        if(data instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) data;
            String itemOwner = templateRecord.getOwner(key);
            if(!itemOwner.equals(templateRecord.getOwner()))
            {
                row.addParameter("inheritedFrom", itemOwner);
                transformDeleteAction(row, "hide", messages);
            }
            else
            {
                TemplateRecord templateParent = templateRecord.getParent();
                if(templateParent != null)
                {
                    String parentItemOwner = templateParent.getOwner(key);
                    if(parentItemOwner != null)
                    {
                        row.addParameter("overriddenOwner", parentItemOwner);
                        transformDeleteAction(row, "hide", messages);
                    }
                }
            }
        }
    }

    private void transformDeleteAction(Row row, String action, Messages messages)
    {
        RowAction deleteAction = row.getAction(AccessManager.ACTION_DELETE);
        if(deleteAction != null)
        {
            deleteAction.setLabel(messages.format(action + ".label"));
        }
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
