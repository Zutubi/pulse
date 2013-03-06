package com.zutubi.pulse.master.tove.webwork;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.i18n.MessagesProvider;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.logging.Logger;

import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Action for the information/action page for a templated record.
 */
public class DisplayTemplateAction extends ActionSupport implements MessagesProvider
{
    private static final Logger LOG = Logger.getLogger(DisplayTemplateAction.class);

    private String path = "";
    private String id;
    private Type type;
    private TemplateNode node;
    private Record record;
    private Record parentRecord;
    private List<Record> childRecords;

    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeRegistry typeRegistry;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getId()
    {
        return id;
    }

    public TemplateNode getNode()
    {
        return node;
    }

    public Record getRecord()
    {
        return record;
    }

    public Record getParentRecord()
    {
        return parentRecord;
    }

    public List<Record> getChildRecords()
    {
        return childRecords;
    }

    public boolean isRootTemplate()
    {
        return node.getParent() == null;
    }

    public boolean isConcrete()
    {
        return node.isConcrete();
    }

    public boolean isPermanent()
    {
        return record.isPermanent();
    }
    
    public Messages getMessages()
    {
        return Messages.getInstance(type.getTargetType().getClazz());
    }

    public String execute() throws Exception
    {
        String[] elements = PathUtils.getPathElements(path);
        if(elements.length != 2)
        {
            addActionError("Invalid path '" + path + "'");
            return ERROR;
        }

        try
        {
            record = configurationTemplateManager.getRecord(path);
            if(record == null)
            {
                addActionError("Unable to find record for path '" + path + "'");
                return ERROR;
            }

            type = typeRegistry.getType(record.getSymbolicName());

            TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(elements[0]);
            id = elements[1];
            node = hierarchy.getNodeById(id);
            if(node == null)
            {
                addActionError("Unable to find template node for path '" + path + "'");
                return ERROR;
            }

            TemplateNode parent = node.getParent();
            if(parent != null)
            {
                parentRecord = configurationTemplateManager.getRecord(parent.getPath());
            }

            List<TemplateNode> children = node.getChildren();
            childRecords = newArrayList(transform(children, new Function<TemplateNode, Record>()
            {
                public Record apply(TemplateNode templateNode)
                {
                    return configurationTemplateManager.getRecord(templateNode.getPath());
                }
            }));

            return SUCCESS;
        }
        catch (IllegalArgumentException e)
        {
            LOG.severe(e);
            addActionError(e.getMessage());
            return ERROR;
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
