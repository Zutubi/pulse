/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.rest.controllers.main;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.PostContext;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.Validation;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.docs.PropertyDocs;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.tove.ui.ConfigModelBuilder;
import com.zutubi.tove.ui.ValidationException;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.model.*;
import com.zutubi.tove.ui.model.forms.CheckboxFieldModel;
import com.zutubi.tove.ui.model.forms.DropdownFieldModel;
import com.zutubi.tove.ui.model.forms.FormModel;
import com.zutubi.tove.ui.wizards.ConfigurationWizard;
import com.zutubi.tove.ui.wizards.DefaultWizard;
import com.zutubi.tove.ui.wizards.WizardContext;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
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
    private static final Logger LOG = Logger.getLogger(WizardController.class);

    private static final String STEP_HIERARCHY = "meta.hierarchy";
    private static final String FIELD_PARENT_TEMPLATE = "parentTemplate";
    private static final String LABEL_PARENT_TEMPLATE = "parent template";
    private static final String FIELD_TEMPLATE = "isTemplate";

    @Autowired
    private ConfigurationSecurityManager configurationSecurityManager;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private ConfigModelBuilder configModelBuilder;
    @Autowired
    private ObjectFactory objectFactory;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ResponseEntity<WizardModel> get(HttpServletRequest request) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);
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
            model = buildModel(context.getPostableType(), context.getParentPath());
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    private WizardModel buildTemplateModel(String scope, String parentName) throws TypeException
    {
        TemplatedMapType collectionType = configurationTemplateManager.getType(scope, TemplatedMapType.class);
        CompositeType itemType = collectionType.getTargetType();
        WizardModel model = buildModel(itemType, scope);

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
        TypeDocs formDocs = new TypeDocs();

        Messages messages = Messages.getInstance(itemType.getClazz());
        form.addField(new DropdownFieldModel(FIELD_PARENT_TEMPLATE, LABEL_PARENT_TEMPLATE, getAllTemplates(hierarchy)));
        formDefaults.put(FIELD_PARENT_TEMPLATE, parentNode.getId());
        formDocs.addProperty(new PropertyDocs(FIELD_PARENT_TEMPLATE, LABEL_PARENT_TEMPLATE, null, messages.format("parent.template.verbose")));

        String templateLabel = "template " + StringUtils.stripSuffix(scope, "s");
        form.addField(new CheckboxFieldModel(FIELD_TEMPLATE, templateLabel));
        formDefaults.put(FIELD_TEMPLATE, false);
        formDocs.addProperty(new PropertyDocs(FIELD_TEMPLATE, templateLabel, messages.format("template.brief"), messages.format("template.verbose")));

        CustomWizardStepModel preludeStep = new CustomWizardStepModel("hierarchy", STEP_HIERARCHY, form);
        preludeStep.setFormDefaults(formDefaults);
        preludeStep.setDocs(formDocs);
        model.prependStep(preludeStep);

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

        Collections.sort(templates, new Sort.StringComparator());
        return templates;
    }

    private WizardModel buildModel(CompositeType type, String parentPath) throws TypeException
    {
        try
        {
            ConfigurationWizard wizard = buildWizard(type);
            FormContext context = new FormContext(parentPath);
            return wizard.buildModel(type, context);
        }
        catch (ValidationException e)
        {
            throw new com.zutubi.pulse.master.rest.errors.ValidationException(e.getInstance(), e.getKey());
        }
    }

    private ConfigurationWizard buildWizard(CompositeType type)
    {
        Class wizardClass = null;
        Wizard annotation = type.getAnnotation(Wizard.class, true);
        if (annotation != null)
        {
            try
            {
                wizardClass = type.getClazz().getClassLoader().loadClass(annotation.value());
            }
            catch (ClassNotFoundException e)
            {
                LOG.warning("Could not load wizard class '" + annotation.value() + "'" + e.getMessage(), e);
            }
        }

        if (wizardClass == null)
        {
            wizardClass = ConventionSupport.loadClass(type, "Wizard", ConfigurationWizard.class);
        }

        if (wizardClass == null)
        {
            wizardClass = DefaultWizard.class;
        }

        return (ConfigurationWizard) objectFactory.buildBean(wizardClass);
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ResponseEntity<ConfigDeltaModel> put(HttpServletRequest request,
                                                @RequestBody Map<String, CompositeModel> body) throws TypeException
    {
        String configPath = Utils.getRequestedPath(request);

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
        ConfigModel model = configModelBuilder.buildModel(null, newPath, -1);
        if (newPath.equals(configPath))
        {
            delta.addUpdatedPath(newPath, model);
        }
        else
        {
            delta.addAddedPath(newPath, model);
        }

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
            throw Validation.newFieldError(FIELD_PARENT_TEMPLATE, "parent template is required");
        }

        Record templateParentRecord = configurationTemplateManager.getRecord(PathUtils.getPath(scope, parentTemplateName.toString()));
        if (templateParentRecord == null)
        {
            throw Validation.newFieldError(FIELD_PARENT_TEMPLATE, "invalid parent template '" + parentTemplateName + "'");
        }

        Object isTemplate = hierarchyDetails.getProperties().get(FIELD_TEMPLATE);
        boolean templated = isTemplate == null ? false : Boolean.valueOf(isTemplate.toString());

        MutableRecord record;
        try
        {
            ConfigurationWizard wizard = buildWizard(itemType);
            record = wizard.buildRecord(itemType, new WizardContext(scope, null, (TemplateRecord) templateParentRecord, null, !templated, body));
        }
        catch (ValidationException e)
        {
            throw new com.zutubi.pulse.master.rest.errors.ValidationException(e.getInstance(), e.getKey());
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
        configurationSecurityManager.ensurePermission(configPath, AccessManager.ACTION_CREATE);

        PostContext context = Utils.getPostContext(configPath, configurationTemplateManager);

        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(configPath);
        try
        {
            ConfigurationWizard wizard = buildWizard(context.getPostableType());
            boolean concrete = templateOwnerPath == null || configurationTemplateManager.isConcrete(templateOwnerPath);
            MutableRecord record = wizard.buildRecord(context.getPostableType(), new WizardContext(configPath, context.getBaseName(), null, templateOwnerPath, concrete, body));

            return configurationTemplateManager.insertRecord(configPath, record);
        }
        catch (ValidationException e)
        {
            throw new com.zutubi.pulse.master.rest.errors.ValidationException(e.getInstance(), e.getKey());
        }
    }
}