package com.zutubi.prototype.table;

import com.zutubi.i18n.Messages;
import com.zutubi.prototype.AbstractParameterised;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.model.ActionLink;
import com.zutubi.prototype.model.Cell;
import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Table;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.bootstrap.SystemPaths;
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
    private ConfigurationProvider configurationProvider;
    private ActionManager actionManager;
    private SystemPaths systemPaths;

    public TableDescriptor(CollectionType collectionType, boolean orderAllowed, boolean addAllowed, ConfigurationProvider configurationProvider, ActionManager actionManager, SystemPaths systemPaths)
    {
        this.collectionType = collectionType;
        this.type = (CompositeType) collectionType.getCollectionType();

        addParameter(PARAM_HEADING, PrototypeUtils.getTableHeading(type));
        addParameter(PARAM_ADD_ALLOWED, addAllowed);
        addParameter(PARAM_ORDER_ALLOWED, orderAllowed && collectionType.isOrdered());

        this.configurationProvider = configurationProvider;
        this.actionManager = actionManager;
        this.systemPaths = systemPaths;
    }

    public Table instantiate(String path, Record data)
    {
        int width = columns.size() + (getParameter(PARAM_ORDER_ALLOWED, false) ? 3 : 2);
        Table table = new Table(width);
        table.addAll(getParameters());

        Messages messages = Messages.getInstance(type.getClazz());

        for (ColumnDescriptor column : columns)
        {
            String key = column.getName() + ".label";
            table.addHeader(PrototypeUtils.format(messages, key));
        }

        if (data != null)
        {
            for (String key : collectionType.getOrder(data))
            {
                String itemPath = PathUtils.getPath(path, key);
                Configuration instance = configurationProvider.get(itemPath, Configuration.class);
                messages = Messages.getInstance(instance.getClass());
                Row row = new Row(itemPath, false, getActions(instance, data, key, messages));
                addCells(row, instance);
                applyRowDecorations(data, key, row);
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
                        Configuration instance = configurationProvider.get(parentItemPath, Configuration.class);
                        messages = Messages.getInstance(instance.getClass());
                        Row row = new Row(PathUtils.getPath(path, hidden), true, Arrays.asList(new ActionLink("restore", PrototypeUtils.format(messages, "restore.label"), "restore")));
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

    private void applyRowDecorations(Record data, String key, Row row)
    {
        if (data instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) data;
            String itemOwner = templateRecord.getOwner(key);
            if (!itemOwner.equals(templateRecord.getOwner()))
            {
                row.addParameter("inheritedFrom", itemOwner);
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
                    }
                }
            }
        }
    }

//    private void transformDeleteAction(Row row, String action, Messages messages)
//    {
//        RowAction deleteAction = row.getAction(AccessManager.ACTION_DELETE);
//        if (deleteAction != null)
//        {
//            deleteAction.setLabel(format(messages, action + ".label"));
//            deleteAction.setIcon(action);
//        }
//    }

    private List<ActionLink> getActions(Object instance, final Record data, final String key, final Messages messages)
    {
        try
        {
            List<String> actionNames = actionManager.getActions((Configuration) instance, true);
            return CollectionUtils.map(actionNames, new Mapping<String, ActionLink>()
            {
                public ActionLink map(String actionName)
                {
                    return PrototypeUtils.getActionLink(actionName, data, key, messages, systemPaths);
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
