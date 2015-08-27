package com.zutubi.pulse.master.rest;

import com.google.common.collect.Sets;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.*;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
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

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_CREATE);

        PostContext context = Utils.getPostContext(configPath, configurationTemplateManager);
        return new ResponseEntity<>(buildModel(context.getPostableType(), context.getParentPath(), context.getBaseName(), configurationTemplateManager.isConcrete(configPath)), HttpStatus.OK);
    }

    private WizardModel buildModel(CompositeType type, String parentPath, String baseName, boolean concrete)
    {
        List<CompositeType> types;
        if (type.isExtendable())
        {
            types = type.getExtensions();
        } else
        {
            types = Collections.singletonList(type);
        }

        WizardModel model = new WizardModel();
        Messages messages = Messages.getInstance(type.getClazz());
        WizardStepModel step = new WizardStepModel("", messages.format("label"));
        for (CompositeType stepType : types)
        {
            messages = Messages.getInstance(stepType.getClazz());
            String labelKey = messages.isKeyDefined("wizard.label") ? "wizard.label" : "label";
            step.addType(new WizardTypeModel(configModelBuilder.buildCompositeTypeModel(parentPath, baseName, stepType, concrete), messages.format(labelKey)));
        }

        model.addStep(step);
        return model;
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ResponseEntity<String> put(HttpServletRequest request,
                                      @RequestBody Map<String, CompositeModel> body) throws TypeException
    {
        String configPath = Utils.getConfigPath(request);
        if (!body.keySet().equals(Sets.newHashSet((""))))
        {
            throw new IllegalArgumentException("Only single step wizards are currently supported");
        }

        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_CREATE);

        PostContext context = Utils.getPostContext(configPath, configurationTemplateManager);
        CompositeType postableType = context.getPostableType();
        CompositeType actualType = null;
        CompositeType postedType = null;

        if (postableType.isExtendable())
        {
            if (postableType.getExtensions().size() == 1)
            {
                actualType = postableType.getExtensions().get(0);
            }
        }
        else
        {
            actualType = postableType;
        }

        String key = "";
        CompositeModel model = body.get(key);
        CompositeTypeModel typeModel = model.getType();
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

        if (!postableType.isAssignableFrom(actualType))
        {
            throw new IllegalArgumentException("Model for key '" + key + "' has incompatible type '" + actualType.getSymbolicName() + "'.");
        }

        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);
        MutableRecord record = Utils.convertProperties(actualType, templateOwnerPath, model.getProperties());

        Configuration instance = configurationTemplateManager.validate(context.getParentPath(), context.getBaseName(), record, configurationTemplateManager.isConcrete(configPath), false);
        if (!instance.isValid())
        {
            throw new ValidationException(instance, key);
        }

        return new ResponseEntity<>(configurationTemplateManager.insertRecord(configPath, record), HttpStatus.OK);
    }
}