package com.zutubi.pulse.master.rest.actions;

import com.google.common.base.Function;
import com.zutubi.pulse.master.rest.Validation;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.ui.model.ActionModel;
import com.zutubi.tove.ui.model.forms.ControllingCheckboxFieldModel;
import com.zutubi.tove.ui.model.forms.FormModel;
import com.zutubi.tove.ui.model.forms.TextFieldModel;
import com.zutubi.tove.validation.NameValidator;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper base class for implementing regular and smart clone handlers, which share very similar
 * forms for selecting descendants to clone.
 */
public abstract class AbstractCloneHandler implements ActionHandler
{
    private static final String FIELD_PARENT_KEY = "parentKey";
    private static final String FIELD_CLONE_KEY = "cloneKey";
    private static final String CHECK_FIELD_PREFIX = "cloneCheck_";
    private static final String KEY_FIELD_PREFIX   = "cloneKey_";

    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ConfigurationRefactoringManager configurationRefactoringManager;

    protected ActionModel getModel(String path, boolean smart)
    {
        final MapType parentType = configurationTemplateManager.getType(PathUtils.getParentPath(path), MapType.class);
        Record record = configurationTemplateManager.getRecord(path);
        String key = parentType.getItemKey(path, record);

        final FormModel form = new FormModel();
        final Map<String, Object> defaults = new HashMap<>();

        form.addField(new TextFieldModel(FIELD_CLONE_KEY, "clone name"));
        defaults.put(FIELD_CLONE_KEY, "clone of " + key);

        if (parentType instanceof TemplatedMapType)
        {
            if (smart)
            {
                form.addField(new TextFieldModel(FIELD_PARENT_KEY, "extracted parent name"));
                defaults.put(FIELD_PARENT_KEY, PathUtils.getBaseName(path) + " template");
            }

            TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
            templateNode.forEachDescendant(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode node)
                {
                    Record descendantRecord = configurationTemplateManager.getRecord(node.getPath());
                    String key = parentType.getItemKey(node.getPath(), descendantRecord);
                    String nameFieldName = KEY_FIELD_PREFIX + key;

                    ControllingCheckboxFieldModel checkbox = new ControllingCheckboxFieldModel(CHECK_FIELD_PREFIX + key, "clone descendant '" + key + "'");
                    checkbox.setCheckedFields(getDependentFields(nameFieldName, node));
                    form.addField(checkbox);

                    form.addField(new TextFieldModel(nameFieldName, "clone name"));
                    defaults.put(nameFieldName, "clone of " + key);

                    return true;
                }
            }, true, null);
        }

        String action = smart ? ConfigurationRefactoringManager.ACTION_SMART_CLONE : ConfigurationRefactoringManager.ACTION_CLONE;
        ActionModel model = new ActionModel(action, smart ? "smart clone" : "clone", null, true);
        model.setForm(form);
        model.setFormDefaults(defaults);
        return model;
    }

    private String[] getDependentFields(String textField, TemplateNode node)
    {
        String[] result = new String[node.getChildren().size() + 1];
        result[0] = textField;
        int i = 1;
        for(TemplateNode child: node.getChildren())
        {
            result[i++] = CHECK_FIELD_PREFIX + child.getId();
        }

        return result;
    }

    private void validateCloneKey(ValidationException e, String parentPath, MapType mapType, String name, String value, Set<String> seenKeys, TextProvider textProvider)
    {
        if (!StringUtils.stringSet(value))
        {
            e.addFieldError(name, "name is required");
        }
        else if (NameValidator.nameContentIsInvalid(value))
        {
            e.addFieldError(name, "name cannot contain forward slashes (/), backward slashes (\\) or dollar signs ($), and must not begin or end with whitespace");
        }
        else
        {
            if (seenKeys.contains(value))
            {
                e.addFieldError(name, "duplicate name, all names must be unique");
            }
            else
            {
                try
                {
                    configurationTemplateManager.validateNameIsUnique(parentPath, value, mapType.getKeyProperty(), textProvider);
                }
                catch (com.zutubi.validation.ValidationException ex)
                {
                    e.addFieldError(name, ex.getMessage());
                }
            }

            seenKeys.add(value);
        }
    }

    private Map<String, String> validate(String parentPath, String baseName, MapType mapType, Map<String, Object> input)
    {
        Map<String, String> keyMap = new HashMap<>();
        ValidationException e = new ValidationException();
        MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());
        Set<String> seenKeys = new HashSet<>();

        for (Map.Entry<String, Object> entry: input.entrySet())
        {
            String key = entry.getKey();
            if (entry.getValue() instanceof String)
            {
                String value = (String) entry.getValue();
                keyMap.put(key, value);
                validateCloneKey(e, parentPath, mapType, key, value, seenKeys, textProvider);
            }
            else
            {
                e.addFieldError(key, "name must be a string");
            }
        }

        if (!keyMap.containsKey(baseName))
        {
            e.addInstanceError("no clone name provided for primary item '" + baseName + "'");
        }

        if (e.hasErrors())
        {
            throw e;
        }

        return keyMap;
    }

    protected ActionResult doAction(String path, Map<String, Object> input, boolean smart)
    {
        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);
        MapType mapType = configurationTemplateManager.getType(parentPath, MapType.class);

        Map<String, String> keyMap = validate(parentPath, baseName, mapType, input);
        if (smart)
        {
            String parentKey = keyMap.remove(FIELD_PARENT_KEY);
            if (parentKey == null)
            {
                throw Validation.newFieldError(FIELD_PARENT_KEY, "extracted parent template name is required");
            }

            configurationRefactoringManager.smartClone(parentPath, baseName, parentKey, keyMap);
        }
        else
        {
            configurationRefactoringManager.clone(parentPath, keyMap);
        }

        return new ActionResult(ActionResult.Status.SUCCESS, "configuration cloned", PathUtils.getPath(parentPath, keyMap.get(baseName)));
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
