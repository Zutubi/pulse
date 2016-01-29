package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.master.rest.ConfigModelBuilder;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.CollectionModel;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.ConfigDeltaModel;
import com.zutubi.pulse.master.rest.model.ConfigModel;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Spring web controller for the /config subset of the RESTish API.
 */
@RestController
@RequestMapping("/config")
public class ConfigController
{
    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private RecordManager recordManager;
    @Autowired
    private ConfigModelBuilder configModelBuilder;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<ConfigModel[]> get(HttpServletRequest request,
                                           @RequestParam(value = "filter", required = false) String[] filters,
                                           @RequestParam(value = "predicate", required = false) String[] predicates,
                                           @RequestParam(value = "depth", required = false, defaultValue = "0") int depth) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);

        filters = canonicaliseFilters(filters);

        List<ConfigModel> models = new ArrayList<>();
        if (configPath.contains(PathUtils.SEPARATOR + PathUtils.WILDCARD_ANY_ELEMENT))
        {
            Set<String> paths = recordManager.selectAll(configPath).keySet();
            configurationSecurityManager.filterPaths("", paths, AccessManager.ACTION_VIEW);
            for (String path: paths)
            {
                ConfigModel model = getConfigModel(path, predicates, filters, depth);
                if (model != null)
                {
                    models.add(model);
                }
            }
        }
        else
        {
            configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_VIEW);
            ConfigModel model = getConfigModel(configPath, predicates, filters, depth);
            if (model != null)
            {
                models.add(model);
            }
        }

        return new ResponseEntity<>(models.toArray(new ConfigModel[models.size()]), HttpStatus.OK);
    }

    private ConfigModel getConfigModel(String configPath, String[] predicates, String[] filters, int depth) throws TypeException
    {
        // We can model anything with a type, even if it is not an existing path yet.
        ComplexType type = Utils.getType(configPath, configurationTemplateManager);

        String parentPath = PathUtils.getParentPath(configPath);
        ComplexType parentType = parentPath == null ? null : configurationTemplateManager.getType(parentPath);
        Record record = configurationTemplateManager.isPersistent(configPath) ? configurationTemplateManager.getRecord(configPath) : null;
        if (predicates != null)
        {
            if (record == null || !recordMatchesPredicates(record, predicates))
            {
                return null;
            }
        }

        return configModelBuilder.buildModel(filters, configPath, type, parentType, record, depth);
    }

    private boolean recordMatchesPredicates(Record record, String[] predicates)
    {
        for (String predicate: predicates)
        {
            String[] parts = StringUtils.split(predicate, '=', false);
            if (parts.length == 2)
            {
                Object value = record.get(parts[0]);
                if (value == null || !value.toString().equals(parts[1]))
                {
                    return false;
                }
            }
        }

        return true;
    }

    // PUT <path> to update composite, or set the order of a collection.
    // POST <path> to add to a collection. Composite paths are errors.
    // DELETE <path> to remove composite or an item from collection.

    @RequestMapping(value = "/**", method = RequestMethod.PUT)
    public ResponseEntity<ConfigDeltaModel> put(HttpServletRequest request,
                                                @RequestBody ConfigModel config,
                                                @RequestParam(value = "depth", required = false, defaultValue = "0") int depth) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);

        Record existingRecord = configurationTemplateManager.getRecord(configPath);
        if (existingRecord == null)
        {
            throw new NotFoundException("Invalid path '" + configPath + "': no existing configuration found (use POST to create new configuration)");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);
        ComplexType type = Utils.getType(configPath, configurationTemplateManager);
        String parentPath = PathUtils.getParentPath(configPath);
        ComplexType parentType = StringUtils.stringSet(parentPath) ? configurationTemplateManager.getType(parentPath) : null;

        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);

            CompositeModel compositeModel = (CompositeModel) config;
            MutableRecord record = Utils.convertProperties(compositeType, templateOwnerPath, compositeModel.getProperties());
            ToveUtils.unsuppressPasswords(existingRecord, record, type, false);

            Configuration instance = configurationTemplateManager.validate(parentPath, PathUtils.getBaseName(configPath), record, configurationTemplateManager.isConcrete(configPath), false);
            if (!instance.isValid())
            {
                throw new ValidationException(instance);
            }

            String newConfigPath = configurationTemplateManager.saveRecord(configPath, record, false);

            ConfigDeltaModel delta = new ConfigDeltaModel();
            Record newRecord = configurationTemplateManager.getRecord(newConfigPath);
            delta.addUpdatedPath(newConfigPath, configModelBuilder.buildModel(null, newConfigPath, compositeType, parentType, newRecord, -1));
            if (!newConfigPath.equals(configPath))
            {
                delta.addRenamedPath(configPath, newConfigPath);
            }

            return new ResponseEntity<>(delta, HttpStatus.OK);
        }
        else
        {
            CollectionModel collectionModel = (CollectionModel) config;
            if (collectionModel.getNested() == null || collectionModel.getNested().isEmpty())
            {
                // Clears any defined order.
                configurationTemplateManager.setOrder(configPath, Collections.<String>emptyList());
            }
            else
            {
                configurationTemplateManager.setOrder(configPath, Lists.newArrayList(Iterables.transform(collectionModel.getNested(), new Function<ConfigModel, String>()
                {
                    @Override
                    public String apply(ConfigModel input)
                    {
                        return input.getKey();
                    }
                })));
            }

            ConfigDeltaModel delta = new ConfigDeltaModel();
            Record newRecord = configurationTemplateManager.getRecord(configPath);
            delta.addUpdatedPath(configPath, configModelBuilder.buildModel(null, configPath, type, parentType, newRecord, -1));

            return new ResponseEntity<>(delta, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.DELETE)
    public ResponseEntity<ConfigDeltaModel> delete(HttpServletRequest request) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);

        Record existingRecord = configurationTemplateManager.getRecord(configPath);
        if (existingRecord == null)
        {
            throw new NotFoundException("Invalid path '" + configPath + "': no configuration found");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_DELETE);

        RecordCleanupTask cleanupTask = configurationTemplateManager.delete(configPath);
        ConfigDeltaModel delta = new ConfigDeltaModel();
        collectCleanupDeltas(cleanupTask, delta);
        return new ResponseEntity<>(delta, HttpStatus.OK);
    }

    private void collectCleanupDeltas(RecordCleanupTask task, ConfigDeltaModel delta) throws TypeException
    {
        switch (task.getCleanupAction())
        {
            case DELETE:
            {
                delta.addDeletedPath(task.getAffectedPath());
                String parentPath = PathUtils.getParentPath(task.getAffectedPath());
                // If the user deleted a composite property, then the path itself still has a type.
                // From the UI POV the path will likely need to be refreshed, so we add an update
                // to the delta on the parent path.
                if (configurationTemplateManager.pathExists(parentPath) && configurationTemplateManager.getType(parentPath) instanceof CompositeType)
                {
                    delta.addUpdatedPath(parentPath, configModelBuilder.buildModel(null, parentPath, -1));
                }
                break;
            }
            case PARENT_UPDATE:
            {
                String parentPath = PathUtils.getParentPath(task.getAffectedPath());
                if (configurationTemplateManager.pathExists(parentPath))
                {
                    delta.addUpdatedPath(parentPath, configModelBuilder.buildModel(null, parentPath, -1));
                }
                break;
            }
            case NONE:
                break;
        }

        for (RecordCleanupTask child: task.getCascaded())
        {
            collectCleanupDeltas(child, delta);
        }
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
}
