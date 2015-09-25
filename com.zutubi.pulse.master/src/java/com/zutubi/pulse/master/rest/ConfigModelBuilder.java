package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.Listing;
import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.ReflectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Builds ConfigModel instances given relevant path and type info.
 */
public class ConfigModelBuilder
{
    private static final Logger LOG = Logger.getLogger(ConfigModelBuilder.class);

    private ActionManager actionManager;
    private ClassificationManager classificationManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private FormModelBuilder formModelBuilder;
    private TableModelBuilder tableModelBuilder;
    private ObjectFactory objectFactory;
    private SystemPaths systemPaths;
    private MasterConfigurationRegistry configurationRegistry;

    public ConfigModel buildModel(String[] filters, String path, ComplexType type, ComplexType parentType, Record record, int depth) throws TypeException
    {
        String label = getLabel(path, type, parentType, record);
        ConfigModel model;
        if (type instanceof CollectionType)
        {
            model = createCollectionModel(path, (CollectionType) type, label, record, filters);
        }
        else
        {
            CompositeType compositeType = (CompositeType) type;
            if (record == null)
            {
                model = createTypeSelectionModel(path, compositeType, label, filters);
            }
            else
            {
                model = createCompositeModel(path, compositeType, label, record, filters);
            }
        }

        model.setIconClass(classificationManager.classify(path));
        if (depth != 0 && record != null)
        {
            model.setNested(getNested(filters, path, type, record, depth - 1));
        }

        return model;
    }

    private boolean isFieldSelected(String[] filters, String fieldName)
    {
        return filters == null || ArrayUtils.contains(filters, fieldName);
    }

    private String getLabel(String path, ComplexType type, ComplexType parentType, Record value)
    {
        String label = ToveUtils.getDisplayName(path, type, parentType, value);
        if (!StringUtils.stringSet(label))
        {
            label = PathUtils.getBaseName(path);
        }
        return label;
    }

    private ConfigModel createCollectionModel(String path, CollectionType type, String label, Record record, String[] filters)
    {
        String baseName = PathUtils.getBaseName(path);
        CollectionModel model = new CollectionModel(baseName, Long.toString(record.getHandle()), label);
        if (isFieldSelected(filters, "table"))
        {
            model.setTable(tableModelBuilder.createTable(type));
        }

        if (isFieldSelected(filters, "type"))
        {
            model.setType(new CollectionTypeModel(type));
        }

        if (isFieldSelected(filters, "allowedActions"))
        {
            if (configurationSecurityManager.hasPermission(path, AccessManager.ACTION_CREATE))
            {
                model.addAllowedAction(AccessManager.ACTION_CREATE);
            }

            if (configurationSecurityManager.hasPermission(path, AccessManager.ACTION_WRITE))
            {
                model.addAllowedAction(AccessManager.ACTION_WRITE);
            }
        }

        return model;
    }

    private ConfigModel createTypeSelectionModel(String path, CompositeType compositeType, String label, String[] filters)
    {
        String baseName = PathUtils.getBaseName(path);
        TypeSelectionModel model = new TypeSelectionModel(baseName, label);
        if (isFieldSelected(filters, "type"))
        {
            model.setType(buildCompositeTypeModel(PathUtils.getParentPath(path), baseName, compositeType, configurationTemplateManager.isConcrete(path)));
        }

        if (isFieldSelected(filters, "configuredDescendants"))
        {
            model.setConfiguredDescendants(getConfiguredDescendants(path));
        }
        return model;
    }

    private List<Pair<Integer, String>> getConfiguredDescendants(String path)
    {
        String elements[] = PathUtils.getPathElements(path);
        if (elements.length >= 2)
        {
            String ownerPath = PathUtils.getPath(0, 2, elements);
            TemplateNode node = configurationTemplateManager.getTemplateNode(ownerPath);
            if (node != null)
            {
                final List<Pair<Integer, String>> result = new ArrayList<>();
                final String remainderPath = PathUtils.getPath(2, elements);
                final int topDepth = node.getDepth() + 1;
                node.forEachDescendant(new Function<TemplateNode, Boolean>()
                {
                    public Boolean apply(TemplateNode currentNode)
                    {
                        String descendantPath = PathUtils.getPath(currentNode.getPath(), remainderPath);
                        if (configurationTemplateManager.pathExists(descendantPath) && configurationSecurityManager.hasPermission(descendantPath, AccessManager.ACTION_VIEW))
                        {
                            result.add(new Pair<>(currentNode.getDepth() - topDepth, currentNode.getId()));
                        }

                        return true;
                    }
                }, true, new NodeIdComparator());

                return result;
            }
        }

        return null;
    }

    private CompositeModel createCompositeModel(String path, CompositeType type, String label, Record record, String[] filters) throws TypeException
    {
        String baseName = PathUtils.getBaseName(path);
        CompositeModel model = new CompositeModel(Long.toString(record.getHandle()), baseName, label);
        Configuration instance = configurationTemplateManager.getInstance(path);
        if (isFieldSelected(filters, "properties"))
        {
            model.setProperties(getProperties(path, type, record));
        }

        if (isFieldSelected(filters, "formattedProperties"))
        {
            model.setFormattedProperties(getFormattedProperties(path, type));
        }

        if (isFieldSelected(filters, "type"))
        {
            model.setType(buildCompositeTypeModel(PathUtils.getParentPath(path), baseName, type, instance.isConcrete()));
        }

        if (isFieldSelected(filters, "actions"))
        {
            addActions(model, path, type, instance);
        }

        return model;
    }

    public CompositeTypeModel buildCompositeTypeModel(String parentPath, String baseName, CompositeType type, boolean concrete)
    {
        CompositeTypeModel typeModel = new CompositeTypeModel(type);
        if (!type.isExtendable())
        {
            typeModel.setForm(formModelBuilder.createForm(parentPath, baseName, type, concrete));

            CompositeType checkType = configurationRegistry.getConfigurationCheckType(type);
            if (checkType != null)
            {
                CompositeTypeModel checkTypeModel = new CompositeTypeModel(checkType);
                checkTypeModel.setForm(formModelBuilder.createForm(parentPath, null, checkType, true));
                typeModel.setCheckType(checkTypeModel);
            }
        }

        List<CompositeType> extensions = type.getExtensions();
        for (CompositeType extension: extensions)
        {
            typeModel.addSubType(buildCompositeTypeModel(parentPath, baseName, extension, concrete));
        }

        return typeModel;
    }

    private Map<String, Object> getProperties(String path, CompositeType type, Record record) throws TypeException
    {
        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);

        // FIXME kendo generalise toXmlRpc if necessary?
        Map<String, Object> result = new HashMap<>();
        for (TypeProperty property: type.getProperties(SimpleType.class))
        {
            Object value = property.getType().toXmlRpc(templateOwnerPath, record.get(property.getName()));
            if (value != null && property.getAnnotation(Password.class) != null)
            {
                value = ToveUtils.SUPPRESSED_PASSWORD;
            }

            result.put(property.getName(), value);
        }

        for (TypeProperty property: type.getProperties(CollectionType.class))
        {
            if (property.getType().getTargetType() instanceof SimpleType)
            {
                Object value = property.getType().toXmlRpc(templateOwnerPath, record.get(property.getName()));
                result.put(property.getName(), value);
            }
        }

        return result;
    }

    private Map<String, Object> getFormattedProperties(String path, CompositeType type) throws TypeException
    {
        // FIXME kendo there is reflection here that could be cached (and moved outside this controller).
        Class<?> formatter = ConventionSupport.getFormatter(type);
        if (formatter != null)
        {
            Configuration instance = configurationTemplateManager.getInstance(path);
            if (instance != null)
            {
                Map<String, Object> properties = new HashMap<>();
                Object formatterInstance = objectFactory.buildBean(formatter);
                for (Method method: formatter.getMethods())
                {
                    if (isGetter(method, type))
                    {
                        try
                        {
                            Object result  = method.invoke(formatterInstance, instance);
                            properties.put(method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4), result);
                        }
                        catch (Exception e)
                        {
                            LOG.severe(e);
                        }
                    }
                }

                if (properties.size() > 0)
                {
                    return properties;
                }
            }
        }

        return null;
    }

    private boolean isGetter(Method method, CompositeType type)
    {
        return method.getName().length() > 3 &&
                method.getName().startsWith("get") &&
                ReflectionUtils.acceptsParameters(method, type.getClazz()) &&
                ReflectionUtils.returnsType(method, Object.class);
    }

    private void addActions(CompositeModel model, String path, CompositeType type, Configuration instance)
    {
        final Messages messages = Messages.getInstance(type.getClazz());
        List<String> actionNames = actionManager.getActions(instance, true, true);

        String key = null;
        Record parentRecord = null;
        String parentPath = PathUtils.getParentPath(path);
        ComplexType parentType = parentPath == null ? null : configurationTemplateManager.getType(parentPath);
        if (parentType != null && parentType instanceof MapType)
        {
            parentRecord = configurationTemplateManager.getRecord(parentPath);
            key = PathUtils.getBaseName(path);
        }

        for (String actionName: actionNames)
        {
            List<String> variants = null;
            if (instance != null)
            {
                variants = actionManager.getVariants(actionName, instance);
            }

            if (variants == null)
            {
                model.addAction(new ActionModel(ToveUtils.getActionLink(actionName, parentRecord, key, messages, systemPaths)));
            }
            else
            {
                for (String variant: variants)
                {
                    model.addAction(new ActionModel(actionName, variant, ToveUtils.getActionIconName(actionName, systemPaths.getContentRoot()), variant));
                }
            }
        }
    }

    private List<ConfigModel> getNested(final String[] filters, final String path, final ComplexType type, final Record record, final int depth)
    {
        List<String> order;
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            order = compositeType.getNestedPropertyNames();
        }
        else
        {
            CollectionType collectionType = (CollectionType) type;
            order = collectionType.getOrder(record);
            configurationSecurityManager.filterPaths(path, order, AccessManager.ACTION_VIEW);
        }

        List<ConfigModel> children = Lists.newArrayList(Lists.transform(order, new Function<String, ConfigModel>()
        {
            @Override
            public ConfigModel apply(String key)
            {
                Record value = (Record) record.get(key);
                ComplexType propertyType = (ComplexType) type.getActualPropertyType(key, value);
                try
                {
                    return buildModel(filters, PathUtils.getPath(path, key), propertyType, type, value, depth);
                }
                catch (TypeException e)
                {
                    throw new PulseRuntimeException(e);
                }
            }
        }));

        if (type instanceof CompositeType)
        {
            // We do this last as it will use the labels we've looked up when creating the children
            // for default ordering.
            sortCompositeChildren((CompositeType)type, children);
        }

        return children;
    }

    private void sortCompositeChildren(CompositeType type, List<ConfigModel> children)
    {
        final Sort.StringComparator stringComparator = new Sort.StringComparator();
        Collections.sort(children, new Comparator<ConfigModel>()
        {
            @Override
            public int compare(ConfigModel c1, ConfigModel c2)
            {
                return stringComparator.compare(c1.getLabel(), c2.getLabel());
            }
        });

        Listing annotation = type.getAnnotation(Listing.class, true);
        if (annotation != null)
        {
            String[] definedOrder = annotation.order();
            int targetIndex = 0;
            for (final String key: definedOrder)
            {
                int sourceIndex = Iterables.indexOf(children, new Predicate<ConfigModel>()
                {
                    @Override
                    public boolean apply(ConfigModel c)
                    {
                        return c.getKey().equals(key);
                    }
                });

                // Note sourceIndex will never be less than targetIndex as we've already filled up
                // to targetIndex - 1 with other items (unless the Listing has dupes I guess).
                if (sourceIndex > targetIndex)
                {
                    children.add(targetIndex, children.remove(sourceIndex));
                    targetIndex++;
                }
            }
        }
    }

    public CleanupTaskModel buildCleanupTask(RecordCleanupTask task)
    {
        CleanupTaskModel model = new CleanupTaskModel(task.getAffectedPath(), Messages.getInstance(task).format("summary"));
        for (RecordCleanupTask child: task.getCascaded())
        {
            if (configurationSecurityManager.hasPermission(child.getAffectedPath(), AccessManager.ACTION_VIEW))
            {
                model.addChild(buildCleanupTask(child));
            }
        }

        return model;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setClassificationManager(ClassificationManager classificationManager)
    {
        this.classificationManager = classificationManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setFormModelBuilder(FormModelBuilder formModelBuilder)
    {
        this.formModelBuilder = formModelBuilder;
    }

    public void setTableModelBuilder(TableModelBuilder tableModelBuilder)
    {
        this.tableModelBuilder = tableModelBuilder;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setConfigurationRegistry(MasterConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    private static class NodeIdComparator implements Comparator<TemplateNode>
    {
        private static final Sort.StringComparator DELEGATE = new Sort.StringComparator();

        public int compare(TemplateNode n1, TemplateNode n2)
        {
            return DELEGATE.compare(n1.getId(), n2.getId());
        }
    }
}
