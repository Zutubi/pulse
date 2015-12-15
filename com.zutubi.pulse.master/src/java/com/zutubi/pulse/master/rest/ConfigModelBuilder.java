package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.format.StateDisplayManager;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.api.ActionVariant;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.links.LinkManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.ReflectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
    private LinkManager linkManager;
    private ObjectFactory objectFactory;
    private SystemPaths systemPaths;
    private MasterConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationDocsManager configurationDocsManager;
    private StateDisplayManager stateDisplayManager;

    public ConfigModel buildModel(String[] filters, String path, int depth) throws TypeException
    {
        String parentPath = PathUtils.getParentPath(path);
        ComplexType type = configurationTemplateManager.getType(path);
        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        Record record = configurationTemplateManager.getRecord(path);
        return buildModel(filters, path, type, parentType, record, depth);
    }

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
            if (configurationTemplateManager.isPersistent(path))
            {
                if (record == null)
                {
                    model = createTypeSelectionModel(path, compositeType, label, filters);
                }
                else
                {
                    model = createCompositeModel(path, compositeType, label, parentType == null || parentType.hasSignificantKeys(), record, filters);
                }
            }
            else
            {
                model = createTransientModel(path, compositeType, label);
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
        return filters == null || ArrayUtils.contains(filters, fieldName.toLowerCase());
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
        boolean concrete = configurationTemplateManager.isConcrete(path);
        boolean deeplyValid = configurationTemplateManager.isDeeplyValid(path);
        CollectionModel model = new CollectionModel(baseName, Long.toString(record.getHandle()), label, concrete, deeplyValid);
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

        if (isFieldSelected(filters, "hiddenItems"))
        {
            addHiddenItems(path, type, record, model);
        }

        if (isFieldSelected(filters, "state"))
        {
            Configuration instance = configurationTemplateManager.getInstance(path);
            if (instance != null)
            {
                Map<String, Object> configState = stateDisplayManager.getConfigState(instance);
                if (configState.size() > 0)
                {
                    model.setState(new StateModel(configState, Messages.getInstance(type.getTargetType().getClazz())));
                }
            }
        }

        return model;
    }

    private void addHiddenItems(String path, CollectionType type, Record record, CollectionModel model)
    {
        if (record instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) record;
            TemplateRecord templateParent = templateRecord.getParent();

            if (templateParent != null)
            {
                String parentId = templateParent.getOwner();
                String[] elements = PathUtils.getPathElements(path);
                String parentPath = PathUtils.getPath(elements[0], parentId, PathUtils.getPath(2, elements));

                List<String> hiddenKeys = new LinkedList<>(templateRecord.getHiddenKeys());
                Collections.sort(hiddenKeys, type.getKeyComparator(record));
                for (String hidden : hiddenKeys)
                {
                    String parentItemPath = PathUtils.getPath(parentPath, hidden);
                    Configuration instance = configurationTemplateManager.getInstance(parentItemPath, Configuration.class);
                    if (instance != null)
                    {
                        model.addHiddenItem(new HiddenItemModel(hidden, templateParent.getOwner(hidden)));
                    }
                }
            }
        }
    }

    private ConfigModel createTypeSelectionModel(String path, CompositeType compositeType, String label, String[] filters)
    {
        String baseName = PathUtils.getBaseName(path);
        String closestExistingPath = path;
        boolean concrete = configurationTemplateManager.isConcrete(closestExistingPath);
        if (configurationTemplateManager.isPersistent(path))
        {
            while (!configurationTemplateManager.pathExists(closestExistingPath))
            {
                closestExistingPath = PathUtils.getParentPath(closestExistingPath);
            }
        }
        else
        {
            closestExistingPath = null;
            concrete = true;
        }

        TypeSelectionModel model = new TypeSelectionModel(baseName, label, concrete);

        if (isFieldSelected(filters, "type"))
        {
            FormContext context = new FormContext(closestExistingPath);
            model.setType(buildCompositeTypeModel(compositeType, context));
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

    private CompositeModel createCompositeModel(String path, CompositeType type, String label, boolean keyed, Record record, String[] filters) throws TypeException
    {
        String baseName = PathUtils.getBaseName(path);
        Configuration instance = configurationTemplateManager.getInstance(path);
        boolean deeplyValid = configurationTemplateManager.isDeeplyValid(path);
        CompositeModel model = new CompositeModel(Long.toString(record.getHandle()), baseName, label, keyed, instance.isConcrete(), deeplyValid);
        if (isFieldSelected(filters, "properties"))
        {
            model.setProperties(getProperties(configurationTemplateManager.getTemplateOwnerPath(path), type, record));
        }

        if (isFieldSelected(filters, "formattedProperties"))
        {
            model.setFormattedProperties(getFormattedProperties(path, type));
        }

        if (isFieldSelected(filters, "validationErrors"))
        {
            model.setValidationErrors(getValidationErrors(instance));
        }

        if (isFieldSelected(filters, "type"))
        {
            model.setType(buildCompositeTypeModel(type, new FormContext(instance)));
            if (record instanceof TemplateRecord)
            {
                decorateForm(model.getType().getForm(), type, (TemplateRecord) record);
            }
        }

        if (isFieldSelected(filters, "actions"))
        {
            addActions(model, path, type, instance);
        }

        if (isFieldSelected(filters, "links"))
        {
            model.setLinks(linkManager.getLinks(instance));
        }

        if (isFieldSelected(filters, "state"))
        {
            Map<String, Object> configState = stateDisplayManager.getConfigState(instance);
            if (configState.size() > 0)
            {
                model.setState(new StateModel(configState, Messages.getInstance(type.getClazz())));
            }
        }

        return model;
    }

    public Map<String, Object> getProperties(String templateOwnerPath, CompositeType type, Record record) throws TypeException
    {
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
        Configuration instance = configurationTemplateManager.getInstance(path);
        if (instance != null)
        {
            Map<String, Object> properties = new HashMap<>();
            Class<?> formatter = ConventionSupport.getFormatter(type);

            if (formatter != null)
            {
                Object formatterInstance = objectFactory.buildBean(formatter);
                for (Method method : formatter.getMethods())
                {
                    if (isGetter(method, type))
                    {
                        try
                        {
                            Object result = method.invoke(formatterInstance, instance);
                            if (result != null)
                            {
                                properties.put(method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4), result);
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.severe(e);
                        }
                    }
                }
            }

            // If there is a table annotation, it could reference transient properties which we
            // include with no extra formatting.
            Table table = type.getAnnotation(Table.class, true);
            if (table != null)
            {
                for (String column: table.columns())
                {
                    if (!properties.containsKey(column) && !type.hasProperty(column))
                    {
                        String methodName = getGetterMethodName(column);
                        try
                        {
                            Method getter = instance.getClass().getMethod(methodName);
                            Object result = getter.invoke(instance);
                            if (result != null)
                            {
                                properties.put(column, result);
                            }
                        }
                        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
                        {
                            // noop
                        }
                    }
                }
            }

            if (properties.size() > 0)
            {
                return properties;
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

    private String getGetterMethodName(String name)
    {
        return "get" + name.substring(0,1).toUpperCase() + name.substring(1);
    }

    private Map<String, List<String>> getValidationErrors(Configuration instance)
    {
        Map<String, List<String>> errors = new HashMap<>();
        if (!instance.getInstanceErrors().isEmpty())
        {
            errors.put("", new ArrayList<>(instance.getInstanceErrors()));
        }

        for (Map.Entry<String, List<String>> entry: instance.getFieldErrors().entrySet())
        {
            if (!entry.getValue().isEmpty())
            {
                errors.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    public CompositeTypeModel buildCompositeTypeModel(CompositeType type, FormContext context)
    {
        CompositeTypeModel typeModel = new CompositeTypeModel(type);
        if (!type.isExtendable())
        {
            typeModel.setForm(formModelBuilder.createForm(type));
            formModelBuilder.applyContextToForm(context, type, typeModel.getForm());

            CompositeType checkType = configurationRegistry.getConfigurationCheckType(type);
            if (checkType != null)
            {
                CompositeTypeModel checkTypeModel = new CompositeTypeModel(checkType);
                checkTypeModel.setForm(formModelBuilder.createForm(checkType));
                typeModel.setCheckType(checkTypeModel);
            }
        }

        List<CompositeType> extensions = type.getExtensions();
        for (CompositeType extension: extensions)
        {
            typeModel.addSubType(buildCompositeTypeModel(extension, context));
        }

        typeModel.setDocs(new DocModel(configurationDocsManager.getDocs(type)));

        return typeModel;
    }

    private void decorateForm(FormModel form, CompositeType type, TemplateRecord record)
    {
        // FIXME kendo does not add empty options to selects, do we need that?
        TemplateRecord parentRecord = record.getParent();

        for (FieldModel field : form.getFields())
        {
            String fieldName = field.getName();

            // Note that if a field has both noInherit and noOverride,
            // noInherit takes precedence.
            if (fieldHasAnnotation(type, fieldName, NoInherit.class))
            {
                field.addParameter("noInherit", "true");
            } else
            {
                String ownerId = record.getOwner(fieldName);
                if (ownerId != null)
                {
                    if (!ownerId.equals(record.getOwner()))
                    {
                        if (fieldHasAnnotation(type, fieldName, NoOverride.class))
                        {
                            // This field should be read-only.
                            field.addParameter("noOverride", "true");
                        } else
                        {
                            field.addParameter("inheritedFrom", ownerId);
                        }
                    } else if (parentRecord != null)
                    {
                        // Check for override
                        String parentOwnerId = parentRecord.getOwner(fieldName);
                        if (parentOwnerId != null)
                        {
                            field.addParameter("overriddenOwner", parentOwnerId);
                            field.addParameter("overriddenValue", parentRecord.get(fieldName));
                        }
                    }
                }
            }
        }
    }

    private boolean fieldHasAnnotation(CompositeType type, String fieldName, Class<? extends Annotation> annotationClass)
    {
        return type.getProperty(fieldName).getAnnotation(annotationClass) != null;
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
            List<ActionVariant> variants = null;
            if (instance != null)
            {
                variants = actionManager.getVariants(actionName, instance);
            }

            if (variants == null)
            {
                model.addAction(new ActionModel(ToveUtils.getActionLink(actionName, parentRecord, key, messages, systemPaths), actionManager.hasArgument(actionName, type)));
            }
            else
            {
                for (ActionVariant variant: variants)
                {
                    model.addAction(new ActionModel(actionName, variant.getName(), variant.getName(), variant.hasArgument()));
                }
            }
        }

        List<String> descendantPaths = configurationTemplateManager.getDescendantPaths(path, true, true, false);
        configurationSecurityManager.filterPaths("", descendantPaths, AccessManager.ACTION_VIEW);
        if (descendantPaths.size() > 0)
        {
            Set<String> actionSet = new HashSet<>();
            for (String descendantPath: descendantPaths)
            {
                Configuration descendantInstance = configurationTemplateManager.getInstance(descendantPath);
                if (descendantInstance != null)
                {
                    actionSet.addAll(actionManager.getActions(descendantInstance, false, false));
                }
            }

            for (String actionName: actionSet)
            {
                model.addDescendantAction(new ActionModel(ToveUtils.getActionLink(actionName, parentRecord, key, messages, systemPaths), false));
            }
        }
    }

    public TransientModel buildTransientModel(Class<? extends Configuration> clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new IllegalArgumentException("Request for model of unregistered class '" + clazz + "'");
        }

        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        if (paths.size() != 1)
        {
            throw new IllegalArgumentException("No unambiguous path found for type '" + type.getSymbolicName() + "'");
        }

        String path = paths.get(0);
        ComplexType parentType = configurationTemplateManager.getType(PathUtils.getParentPath(path));
        return createTransientModel(path, type, ToveUtils.getDisplayName(path, type, parentType, null));
    }

    private TransientModel createTransientModel(String path, CompositeType compositeType, String label)
    {
        TransientModel model = new TransientModel(PathUtils.getBaseName(path), label);
        model.setType(buildCompositeTypeModel(compositeType, new FormContext((String) null)));
        return model;
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

    public void setLinkManager(LinkManager linkManager)
    {
        this.linkManager = linkManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationDocsManager(ConfigurationDocsManager configurationDocsManager)
    {
        this.configurationDocsManager = configurationDocsManager;
    }

    public void setStateDisplayManager(StateDisplayManager stateDisplayManager)
    {
        this.stateDisplayManager = stateDisplayManager;
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
