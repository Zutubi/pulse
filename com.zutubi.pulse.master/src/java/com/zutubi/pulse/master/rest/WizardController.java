package com.zutubi.pulse.master.rest;

import com.google.common.collect.Sets;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * RESTish controller for wizards: structured UIs for creating configuration. These are intended
 * only to back GUIs, for purely programmatic config creation {@link ConfigController} is
 * sufficient.
 */
@RestController
@RequestMapping("/wizard")
public class WizardController
{
    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private ConfigModelBuilder configModelBuilder;
    @Autowired
    private TypeRegistry typeRegistry;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<WizardModel> get(HttpServletRequest request) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);
        WizardModel model;

        String[] configPathElements = PathUtils.getPathElements(configPath);
        String scope = configPathElements[0];
        if (configPathElements.length <= 2 && configurationTemplateManager.isTemplatedCollection(scope))
        {
            configurationSecurityManager.ensurePermission(scope, AccessManager.ACTION_CREATE);

            model = buildTemplateModel(scope, configPathElements.length > 1 ? configPathElements[1] : null);
        }
        else
        {
            configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_CREATE);

            PostContext context = Utils.getPostContext(configPath, configurationTemplateManager);
            model = buildModel(context.getPostableType(), context.getParentPath(), context.getBaseName(), configurationTemplateManager.isConcrete(configPath));
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    private WizardModel buildTemplateModel(String scope, String parentName)
    {
        WizardModel model = new WizardModel();
        TemplatedMapType collectionType = configurationTemplateManager.getType(scope, TemplatedMapType.class);
        CompositeType itemType = collectionType.getTargetType();
        model.addStep(buildStepForType(itemType, scope, null, true));
        return model;
    }

    private WizardModel buildModel(CompositeType type, String parentPath, String baseName, boolean concrete)
    {
        WizardModel model = new WizardModel();
        model.addStep(buildStepForType(type, parentPath, baseName, concrete));
        return model;
    }

    private TypedWizardStepModel buildStepForType(CompositeType type, String parentPath, String baseName, boolean concrete)
    {
        List<CompositeType> types;
        if (type.isExtendable())
        {
            types = type.getExtensions();
        } else
        {
            types = Collections.singletonList(type);
        }

        Messages messages = Messages.getInstance(type.getClazz());
        TypedWizardStepModel step = new TypedWizardStepModel("", messages.format("label"));
        for (CompositeType stepType : types)
        {
            messages = Messages.getInstance(stepType.getClazz());
            String labelKey = messages.isKeyDefined("wizard.label") ? "wizard.label" : "label";
            step.addType(new WizardTypeModel(configModelBuilder.buildCompositeTypeModel(parentPath, baseName, stepType, concrete), messages.format(labelKey)));
        }
        return step;
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ResponseEntity<ConfigDeltaModel> put(HttpServletRequest request,
                                                @RequestBody Map<String, CompositeModel> body) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);

        String[] configPathElements = PathUtils.getPathElements(configPath);
        String scope = configPathElements[0];
        String newPath;
        if (configPathElements.length <= 2 && configurationTemplateManager.isTemplatedCollection(scope))
        {
            newPath = insertTemplatedConfig(scope, body);
        }
        else
        {
            newPath = insertConfig(configPath, body);
        }

        ConfigDeltaModel delta = new ConfigDeltaModel();
        delta.addAddedPath(newPath, configModelBuilder.buildModel(null, newPath, -1));
        return new ResponseEntity<>(delta, HttpStatus.OK);
    }

    private String insertTemplatedConfig(String scope, Map<String, CompositeModel> body) throws TypeException
    {
        TemplatedMapType collectionType = configurationTemplateManager.getType(scope, TemplatedMapType.class);
        CompositeType itemType = collectionType.getTargetType();

        boolean templated = false;

        String key = "";
        CompositeModel model = body.get(key);
        MutableRecord record = createRecord(null, itemType, key, model);

        Configuration instance = configurationTemplateManager.validate(scope, null, record, !templated, false);
        if (!instance.isValid())
        {
            throw new ValidationException(instance, key);
        }

        TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(scope);
        Record templateParentRecord = configurationTemplateManager.getRecord(PathUtils.getPath(scope, hierarchy.getRoot().getId()));
        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if (templated)
        {
            configurationTemplateManager.markAsTemplate(record);
        }

        return configurationTemplateManager.insertRecord(scope, record);
    }

    private String insertConfig(String configPath, @RequestBody Map<String, CompositeModel> body) throws TypeException
    {
        if (!body.keySet().equals(Sets.newHashSet((""))))
        {
            throw new IllegalArgumentException("Only single step wizards are currently supported");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_CREATE);

        PostContext context = Utils.getPostContext(configPath, configurationTemplateManager);

        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);
        String key = "";
        CompositeModel model = body.get(key);
        MutableRecord record = createRecord(templateOwnerPath, context.getPostableType(), key, model);

        Configuration instance = configurationTemplateManager.validate(configPath, null, record, configurationTemplateManager.isConcrete(configPath), false);
        if (!instance.isValid())
        {
            throw new ValidationException(instance, key);
        }

        return configurationTemplateManager.insertRecord(configPath, record);
    }

    private MutableRecord createRecord(String templateOwnerPath, CompositeType expectedType, String key, CompositeModel model) throws TypeException
    {
        CompositeType actualType = null;
        CompositeType postedType = null;
        CompositeTypeModel typeModel = model.getType();

        if (expectedType.isExtendable())
        {
            if (expectedType.getExtensions().size() == 1)
            {
                actualType = expectedType.getExtensions().get(0);
            }
        }
        else
        {
            actualType = expectedType;
        }

        if (typeModel != null && typeModel.getSymbolicName() != null)
        {
            postedType = typeRegistry.getType(typeModel.getSymbolicName());
            if (postedType == null)
            {
                throw new IllegalArgumentException("Model for key '" + key + "' has type with unknown symbolic name '" + typeModel.getSymbolicName() + "'");
            }
        }

        if (actualType == null)
        {
            if (postedType == null)
            {
                throw new IllegalArgumentException("Model for key '" + key + "' requires a type with symbolic name.");
            }

            actualType = postedType;
        }

        if (!expectedType.isAssignableFrom(actualType))
        {
            throw new IllegalArgumentException("Model for key '" + key + "' has incompatible type '" + actualType.getSymbolicName() + "'.");
        }

        return Utils.convertProperties(actualType, templateOwnerPath, model.getProperties());
    }
}