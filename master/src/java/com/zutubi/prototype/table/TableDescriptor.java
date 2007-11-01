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
 */
public class TableDescriptor extends AbstractParameterised implements Descriptor
{
    private static final Logger LOG = Logger.getLogger(TableDescriptor.class);

    public static final String PARAM_HEADING = "heading";
    public static final String PARAM_ADD_ALLOWED = "addAllowed";
    public static final String PARAM_ORDER_ALLOWED = "orderAllowed";

    private CollectionType collectionType;
    private CompositeType type;
    /**
     * The columns descriptors associated with this table descriptor.
     */
    private List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();
    private ConfigurationTemplateManager configurationTemplateManager;
    private ActionManager actionManager;

    public TableDescriptor(CollectionType collectionType, boolean orderAllowed, boolean addAllowed, ConfigurationTemplateManager configurationTemplateManager, ActionManager actionManager)
    {
        this.collectionType = collectionType;
        this.type = (CompositeType) collectionType.getCollectionType();

        addParameter(PARAM_HEADING, PrototypeUtils.getTableHeading(type));
        addParameter(PARAM_ADD_ALLOWED, addAllowed);
        addParameter(PARAM_ORDER_ALLOWED, orderAllowed && collectionType.isOrdered());

        this.configurationTemplateManager = configurationTemplateManager;
        this.actionManager = actionManager;
    }

    public Table instantiate(String path, Record data)
    {
        int width = columns.size() + (getParameter(PARAM_ORDER_ALLOWED, false) ? 3 : 2);
        Table table = new Table(width);
        table.addAll(parameters);

        Messages messages = Messages.getInstance(type.getClazz());

        for (ColumnDescriptor column : columns)
        {
            String key = column.getName() + ".label";
            table.addHeader(format(messages, key));
        }

        if (data != null)
        {
            for (String key : collectionType.getOrder(data))
            {
                String itemPath = PathUtils.getPath(path, key);
                Configuration instance = configurationTemplateManager.getInstance(itemPath);
                messages = Messages.getInstance(instance.getClass());
                Row row = new Row(itemPath, false, getActions(instance, messages));
                addCells(row, instance);
                applyRowDecorations(data, key, row, messages);
                table.addRow(row);
            }

            if (data instanceof TemplateRecord)
            {
                TemplateRecord templateRecord = (TemplateRecord) data;
                TemplateRecord templateParent = templateRecord.getParent();

                applyTableDecorations(table, templateRecord, templateParent);

                if (templateParent != null)
                {
                    String parentId = templateParent.getOwner();
                    String[] elements = PathUtils.getPathElements(path);
                    String parentPath = PathUtils.getPath(elements[0], parentId, PathUtils.getPath(2, elements));

                    List<String> hiddenKeys = new LinkedList<String>(templateRecord.getHiddenKeys());
                    Collections.sort(hiddenKeys, collectionType.getKeyComparator(data));
                    for (String hidden : hiddenKeys)
                    {
                        String parentItemPath = PathUtils.getPath(parentPath, hidden);
                        Configuration instance = configurationTemplateManager.getInstance(parentItemPath);
                        messages = Messages.getInstance(instance.getClass());
                        Row row = new Row(PathUtils.getPath(path, hidden), true, Arrays.asList(new RowAction("restore", format(messages, "restore.label"))));
                        addCells(row, instance);
                        row.addParameter("hiddenFrom", templateParent.getOwner(hidden));
                        row.addParameter("cls", "item-hidden");
                        table.addRow(row);
                    }
                }
            }
        }

        return table;
    }

    private void applyTableDecorations(Table table, TemplateRecord templateRecord, TemplateRecord templateParent)
    {
        if (table.isOrderable())
        {
            String owner = templateRecord.getOwner();
            if (owner != null)
            {
                String orderOwner = templateRecord.getMetaOwner(CollectionType.ORDER_KEY);
                if (!owner.equals(orderOwner))
                {
                    table.addParameter("orderInheritedFrom", orderOwner);
                }
                else if (templateParent != null)
                {
                    String parentOrderOwner = templateParent.getMetaOwner(CollectionType.ORDER_KEY);
                    if(parentOrderOwner != null)
                    {
                        table.addParameter("orderOverriddenOwner", parentOrderOwner);
                    }
                }
            }
        }
    }

    private void addCells(Row row, Configuration instance)
    {
        for (ColumnDescriptor column : columns)
        {
            row.addCell(new Cell(column.getValue(instance).toString()));
        }
    }

    private void applyRowDecorations(Record data, String key, Row row, Messages messages)
    {
        if (data instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) data;
            String itemOwner = templateRecord.getOwner(key);
            if (!itemOwner.equals(templateRecord.getOwner()))
            {
                row.addParameter("inheritedFrom", itemOwner);
                transformDeleteAction(row, "hide", messages);
            }
            else
            {
                TemplateRecord templateParent = templateRecord.getParent();
                if (templateParent != null)
                {
                    String parentItemOwner = templateParent.getOwner(key);
                    if (parentItemOwner != null)
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
        if (deleteAction != null)
        {
            deleteAction.setLabel(format(messages, action + ".label"));
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
                    return new RowAction(actionName, format(messages, actionName + ".label"));
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

    private String format(Messages messages, String key)
    {
        String value = messages.format(key);
        if(value == null)
        {
            value = key;
        }
        return value;
    }
}
