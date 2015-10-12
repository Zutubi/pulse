package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.pulse.master.rest.model.forms.CheckboxFieldModel;
import com.zutubi.pulse.master.rest.model.forms.DropdownFieldModel;
import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * RESTish controller for wizards: structured UIs for creating configuration. These are intended
 * only to back GUIs, for purely programmatic config creation {@link ConfigController} is
 * sufficient.
 */
@RestController
@RequestMapping("/wizard")
public class WizardController
{
    private static final String STEP_HIERARCHY = "meta.hierarcy";
    private static final String FIELD_PARENT_TEMPLATE = "parentTemplate";
    private static final String FIELD_TEMPLATE = "isTemplate";

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
        TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(scope);
        TemplateNode parentNode = hierarchy.getRoot();
        if (StringUtils.stringSet(parentName))
        {
            TemplateNode node = hierarchy.getNodeById(parentName);
            if (node != null)
            {
                while (node.isConcrete())
                {
                    node = node.getParent();
                }

                parentNode = node;
            }
        }

        FormModel form = new FormModel();
        Map<String, Object> formDefaults = new HashMap<>();

        form.addField(new DropdownFieldModel(FIELD_PARENT_TEMPLATE, "parent template", getAllTemplates(hierarchy)));
        formDefaults.put(FIELD_PARENT_TEMPLATE, parentNode.getId());
        form.addField(new CheckboxFieldModel(FIELD_TEMPLATE, "template project"));
        formDefaults.put(FIELD_TEMPLATE, false);

        CustomWizardStepModel preludeStep = new CustomWizardStepModel("hierarcy", STEP_HIERARCHY, form);
        preludeStep.setFormDefaults(formDefaults);
        model.addStep(preludeStep);

        TemplatedMapType collectionType = configurationTemplateManager.getType(scope, TemplatedMapType.class);
        CompositeType itemType = collectionType.getTargetType();
        model.addStep(buildStepForType(itemType, scope, null, true));
        return model;
    }

    private List getAllTemplates(TemplateHierarchy hierarchy)
    {
        final List<String> templates = new ArrayList<>();
        hierarchy.getRoot().forEachDescendant(new Function<TemplateNode, Boolean>()
        {
            @Override
            public Boolean apply(TemplateNode node)
            {
                if (!node.isConcrete())
                {
                    templates.add(node.getId());
                }
                return true;
            }
        }, false, null);

        templates.sort(new Sort.StringComparator());
        return templates;
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


        CompositeModel hierarchyDetails = body.get(STEP_HIERARCHY);
        if (hierarchyDetails == null)
        {
            throw new IllegalArgumentException("Missing hierarchy details, should be included using key " + STEP_HIERARCHY);
        }

        Object parentTemplateName = hierarchyDetails.getProperties().get(FIELD_PARENT_TEMPLATE);
        if (parentTemplateName == null)
        {
            ValidationException e = new ValidationException(null, STEP_HIERARCHY);
            e.addFieldError(FIELD_PARENT_TEMPLATE, "parent template is required");
            throw e;
        }

        Record templateParentRecord = configurationTemplateManager.getRecord(PathUtils.getPath(scope, parentTemplateName.toString()));
        if (templateParentRecord == null)
        {
            ValidationException e = new ValidationException(null, STEP_HIERARCHY);
            e.addFieldError(FIELD_PARENT_TEMPLATE, "invalid parent template '" + parentTemplateName + "'");
            throw e;
        }

        Object isTemplate = hierarchyDetails.getProperties().get(FIELD_TEMPLATE);
        boolean templated = isTemplate == null ? false : Boolean.valueOf(isTemplate.toString());

        String key = "";
        CompositeModel model = body.get(key);
        MutableRecord record = createRecord(null, itemType, key, model);

        Configuration instance = configurationTemplateManager.validate(scope, null, record, !templated, false);
        if (!instance.isValid())
        {
            throw new ValidationException(instance, key);
        }

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