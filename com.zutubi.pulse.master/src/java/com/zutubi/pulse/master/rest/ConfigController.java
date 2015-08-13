package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.Listing;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Spring web controller for the /config subset of the RESTish API.
 */
@RestController
@RequestMapping("/config")
public class ConfigController
{
    @Autowired
    private ActionManager actionManager;
    @Autowired
    private ClassificationManager classificationManager;
    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private FormModelBuilder formModelBuilder;
    @Autowired
    private SystemPaths systemPaths;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<ConfigModel[]> get(HttpServletRequest request,
                                           @RequestParam(value = "filter", required = false) String[] filters,
                                           @RequestParam(value = "depth", required = false, defaultValue = "0") int depth) throws TypeException
    {
        String configPath = getConfigPath(request);

        filters = canonicaliseFilters(filters);

        // We can model anything with a type, even if it is not an existing path yet.
        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_VIEW);
        ComplexType type = configurationTemplateManager.getType(configPath);
        String parentPath = PathUtils.getParentPath(configPath);
        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);
        ComplexType parentType = parentPath == null ? null : configurationTemplateManager.getType(parentPath);
        Configuration instance = configurationTemplateManager.getInstance(configPath);
        if (instance == null)
        {
            throw new NotFoundException("Configuration path '" + configPath + "' not found");
        }

        Object unstantiated = type.unstantiate(instance, templateOwnerPath);
        if (!(unstantiated instanceof MutableRecord))
        {
            throw new NotFoundException("Path '" + configPath + "' does not refer to a selectable resource");
        }

        ConfigModel model = createModel(filters, configPath, type, parentType, (MutableRecord) unstantiated, depth);

        return new ResponseEntity<>(new ConfigModel[]{model}, HttpStatus.OK);
    }

    // PUT <path> to update composite. Collection paths are errors.
    // POST <path> to add to a collection. Composite paths are errors.
    // DELETE <path> to remove composite or an item from collection.

    @RequestMapping(value = "/**", method = RequestMethod.PUT)
    public ResponseEntity<String> put(HttpServletRequest request,
                                      @RequestBody CompositeModel config,
                                      @RequestParam(value = "depth", required = false, defaultValue = "0") int depth) throws TypeException
    {
        String configPath = getConfigPath(request);

        Record existingRecord = configurationTemplateManager.getRecord(configPath);
        if (existingRecord == null)
        {
            throw new NotFoundException("Invalid path '" + configPath + "': no existing configuration found (use POST to create new configuration)");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);
        ComplexType type = configurationTemplateManager.getType(configPath);
        if (!(type instanceof CompositeType))
        {
            throw new IllegalArgumentException("Configuration path '" + configPath + "' refers to unsupported type " + type.toString() + " (PUT is only supported for composite types)");
        }

        CompositeType compositeType = (CompositeType) type;
        String parentPath = PathUtils.getParentPath(configPath);
        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);

        MutableRecord record = convertProperties(compositeType, templateOwnerPath, config.getProperties());
        ToveUtils.unsuppressPasswords(existingRecord, record, type, true);

        Configuration instance = configurationTemplateManager.validate(parentPath, PathUtils.getBaseName(configPath), record, configurationTemplateManager.isConcrete(configPath), false);
        if (!instance.isValid())
        {
            throw new ValidationException(compositeType, instance);
        }

        return new ResponseEntity<>(configurationTemplateManager.saveRecord(configPath, record, false), HttpStatus.OK);
    }

    private MutableRecord convertProperties(CompositeType type, String templateOwnerPath, Map<String, Object> properties) throws TypeException
    {
        MutableRecord result = type.createNewRecord(true);

        // Internal properties may not be set this way, so strip them from the default config.
        for (TypeProperty property: type.getInternalProperties())
        {
            result.remove(property.getName());
        }

        for (TypeProperty property: type.getProperties(SimpleType.class))
        {
            Object value = properties.get(property.getName());
            if (value != null)
            {
                result.put(property.getName(), property.getType().fromXmlRpc(templateOwnerPath, value, true));
            }
        }

        for (TypeProperty property: type.getProperties(CollectionType.class))
        {
            if (property.getType().getTargetType() instanceof SimpleType)
            {
                Object value = properties.get(property.getName());
                if (value != null)
                {
                    result.put(property.getName(), property.getType().fromXmlRpc(templateOwnerPath, value, true));
                }
            }
        }

        return result;
    }

    private String getConfigPath(HttpServletRequest request)
    {
        String requestPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        return apm.extractPathWithinPattern(bestMatchPattern, requestPath);
    }

    private String[] canonicaliseFilters(String[] filters)
    {
        if (filters != null)
        {
            String[] converted = new String[filters.length];
            for (int i = 0; i < filters.length; i++)
            {
                converted[i] = filters[i].toLowerCase();
            }

            filters = converted;
        }
        return filters;
    }

    private boolean isFieldSelected(String[] filters, String fieldName)
    {
        return filters == null || ArrayUtils.contains(filters, fieldName);
    }

    private ConfigModel createModel(String[] filters, String path, ComplexType type, ComplexType parentType, MutableRecord record, int depth) throws TypeException
    {
        String baseName = PathUtils.getBaseName(path);
        String label = getLabel(path, type, parentType, record);
        ConfigModel model;
        if (type instanceof CollectionType)
        {
            model = new CollectionModel(baseName, Long.toString(record.getHandle()), label);
            if (isFieldSelected(filters, "type"))
            {
                model.setType(new CollectionTypeModel((CollectionType) type));
            }
        }
        else
        {
            CompositeType compositeType = (CompositeType) type;
            if (record == null)
            {
                model = createTypeSelectionModel(filters, path, label, compositeType);
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

    private String getLabel(String path, ComplexType type, ComplexType parentType, Record value)
    {
        String label = ToveUtils.getDisplayName(path, type, parentType, value);
        if (!StringUtils.stringSet(label))
        {
            label = PathUtils.getBaseName(path);
        }
        return label;
    }

    private ConfigModel createTypeSelectionModel(String[] filters, String path, String label, CompositeType compositeType)
    {
        TypeSelectionModel model = new TypeSelectionModel(PathUtils.getBaseName(path), label);
        if (isFieldSelected(filters, "type"))
        {
            model.setType(new CompositeTypeModel(compositeType));
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

    private CompositeModel createCompositeModel(String path, CompositeType type, String label, MutableRecord record, String[] filters) throws TypeException
    {
        String baseName = PathUtils.getBaseName(path);
        CompositeModel model = new CompositeModel(Long.toString(record.getHandle()), baseName, label);
        Configuration instance = configurationTemplateManager.getInstance(path);
        if (isFieldSelected(filters, "properties"))
        {
            model.setProperties(getProperties(path, type, record));
        }

        if (isFieldSelected(filters, "type"))
        {
            model.setType(new CompositeTypeModel(type));
        }

        if (isFieldSelected(filters, "form"))
        {
            model.setForm(formModelBuilder.createForm(PathUtils.getParentPath(path), baseName, type, instance.isConcrete(), "form"));
        }

        if (isFieldSelected(filters, "actions"))
        {
            addActions(model, path, type, instance);
        }

        return model;
    }

    private Map<String, Object> getProperties(String path, CompositeType type, MutableRecord record) throws TypeException
    {
        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
        ToveUtils.suppressPasswords(record, type, false);

        // FIXME kendo generalise toXmlRpc if necessary?
        Map<String, Object> result = new HashMap<>();
        for (TypeProperty property: type.getProperties(SimpleType.class))
        {
            Object value = property.getType().toXmlRpc(templateOwnerPath, record.get(property.getName()));
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

    private void addActions(CompositeModel model, String path, CompositeType type, Configuration instance)
    {
        final Messages messages = Messages.getInstance(type.getClazz());
        List<String> actionNames = actionManager.getActions(instance, true, true);

        String key = null;
        Record parentRecord = null;
        String parentPath = PathUtils.getParentPath(path);
        ComplexType parentType = configurationTemplateManager.getType(parentPath);
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

    private List<ConfigModel> getNested(final String[] filters, final String path, final ComplexType type, final MutableRecord record, final int depth)
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
                MutableRecord value = (MutableRecord) record.get(key);
                ComplexType propertyType = (ComplexType) type.getActualPropertyType(key, value);
                try
                {
                    return createModel(filters, PathUtils.getPath(path, key), propertyType, type, value, depth);
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

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
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
