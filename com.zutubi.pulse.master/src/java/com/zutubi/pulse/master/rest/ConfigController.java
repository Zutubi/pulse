package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
    private ConfigModelBuilder configModelBuilder;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<ConfigModel[]> get(HttpServletRequest request,
                                           @RequestParam(value = "filter", required = false) String[] filters,
                                           @RequestParam(value = "depth", required = false, defaultValue = "0") int depth) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);

        filters = canonicaliseFilters(filters);

        // We can model anything with a type, even if it is not an existing path yet.
        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_VIEW);
        ComplexType type = Utils.getType(configPath, configurationTemplateManager);

        String parentPath = PathUtils.getParentPath(configPath);
        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);
        ComplexType parentType = parentPath == null ? null : configurationTemplateManager.getType(parentPath);
        Configuration instance = configurationTemplateManager.getInstance(configPath);
        Object unstantiated = null;

        if (instance != null)
        {
            unstantiated = type.unstantiate(instance, templateOwnerPath);
            if (!(unstantiated instanceof MutableRecord))
            {
                throw new NotFoundException("Path '" + configPath + "' does not refer to a selectable resource");
            }
        }

        ConfigModel model = configModelBuilder.buildModel(filters, configPath, type, parentType, (MutableRecord) unstantiated, depth);

        return new ResponseEntity<>(new ConfigModel[]{model}, HttpStatus.OK);
    }

    // PUT <path> to update composite, or set the order of a collection.
    // POST <path> to add to a collection. Composite paths are errors.
    // DELETE <path> to remove composite or an item from collection.

    @RequestMapping(value = "/**", method = RequestMethod.PUT)
    public ResponseEntity<ConfigDeltaModel> put(HttpServletRequest request,
                                                @RequestBody ConfigModel config,
                                                @RequestParam(value = "depth", required = false, defaultValue = "0") int depth) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);

        Record existingRecord = configurationTemplateManager.getRecord(configPath);
        if (existingRecord == null)
        {
            throw new NotFoundException("Invalid path '" + configPath + "': no existing configuration found (use POST to create new configuration)");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_WRITE);
        ComplexType type = Utils.getType(configPath, configurationTemplateManager);
        String parentPath = PathUtils.getParentPath(configPath);
        ComplexType parentType = configurationTemplateManager.getType(parentPath);

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
            if (collectionModel.getNested() == null)
            {
                throw new IllegalArgumentException("Collection does not have nested records, nothing to save");
            }

            configurationTemplateManager.setOrder(configPath, Lists.newArrayList(Iterables.transform(collectionModel.getNested(), new Function<ConfigModel, String>()
            {
                @Override
                public String apply(ConfigModel input)
                {
                    return input.getKey();
                }
            })));

            ConfigDeltaModel delta = new ConfigDeltaModel();
            Record newRecord = configurationTemplateManager.getRecord(configPath);
            delta.addUpdatedPath(configPath, configModelBuilder.buildModel(null, configPath, type, parentType, newRecord, -1));

            return new ResponseEntity<>(delta, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.DELETE)
    public ResponseEntity<ConfigDeltaModel> delete(HttpServletRequest request) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);

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
