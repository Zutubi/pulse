package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.tove.model.ControllingCheckboxFieldDescriptor;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Action to gather new keys and request a clone for map items.
 */
public class CloneAction extends ToveFormActionSupport
{
    public static final String CHECK_FIELD_PREFIX = "cloneCheck_";
    public static final String KEY_FIELD_PREFIX   = "cloneKey_";

    private Record record;
    private String parentPath;
    private MapType mapType;
    private boolean templatedCollection;
    private boolean smart;
    private String cloneKey;
    private String parentKey;
    private Map<String, String> keyMap;

    private ConfigurationRefactoringManager configurationRefactoringManager;

    public CloneAction()
    {
        super(ConfigurationRefactoringManager.ACTION_CLONE, "clone");
    }

    public boolean isSmart()
    {
        return smart;
    }

    public void setSmart(boolean smart)
    {
        this.smart = smart;
    }

    public void setCloneKey(String cloneKey)
    {
        this.cloneKey = cloneKey;
    }

    public void setParentKey(String parentKey)
    {
        this.parentKey = parentKey;
    }

    @Override
    public void doCancel()
    {
        String parentPath = PathUtils.getParentPath(path);
        String newPath = configurationTemplateManager.isTemplatedCollection(parentPath) ? path : parentPath;
        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
    }

    @Override
    protected void validatePath()
    {
        parentPath = PathUtils.getParentPath(path);
        if(parentPath == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': no parent path");
        }

        Type parentType = configurationTemplateManager.getType(parentPath);
        if(!(parentType instanceof MapType))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': parent is not a map (only map elements may be cloned)");
        }

        mapType = (MapType) parentType;
        type = parentType.getTargetType();
        templatedCollection = configurationTemplateManager.isTemplatedCollection(parentPath);

        record = configurationTemplateManager.getRecord(path);
    }

    @Override
    protected void validateForm()
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());
        Set<String> seenKeys = new HashSet<String>();
        validateCloneKey("cloneKey", cloneKey, seenKeys, textProvider);

        keyMap = new HashMap<String, String>();
        keyMap.put(PathUtils.getBaseName(path), cloneKey);

        if(templatedCollection)
        {
            if(smart)
            {
                validateCloneKey("parentKey", parentKey, seenKeys, textProvider);
            }

            getDescendents(keyMap, seenKeys, textProvider);
        }
    }

    private void validateCloneKey(String name, String value, Set<String> seenKeys, TextProvider textProvider)
    {
        if(!StringUtils.stringSet(value))
        {
            addFieldError(name, "name is required");
        }
        else
        {
            if(seenKeys.contains(value))
            {
                addFieldError(name, "duplicate name, all names must be unique");
            }
            else
            {
                try
                {
                    configurationTemplateManager.validateNameIsUnique(parentPath, value, mapType.getKeyProperty(), textProvider);
                }
                catch(ValidationException e)
                {
                    addFieldError(name, e.getMessage());
                }
            }

            seenKeys.add(value);
        }
    }

    private Map<String, String> getDescendents(Map<String, String> selectedDescedents, Set<String> seenKeys, TextProvider textProvider)
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(CHECK_FIELD_PREFIX))
            {
                String descendentKey = name.substring(CHECK_FIELD_PREFIX.length());
                String value = getParameterValue(parameters, KEY_FIELD_PREFIX + descendentKey);

                if (value != null)
                {
                    validateCloneKey(KEY_FIELD_PREFIX + descendentKey, value, seenKeys, textProvider);
                    selectedDescedents.put(descendentKey, value);
                }
            }
        }

        return selectedDescedents;
    }

    @Override
    protected String getFormAction()
    {
        return smart ? "smartclone" : "clone";
    }

    @Override
    protected void addFormFields(Form form)
    {
        Map parameters = ActionContext.getContext().getParameters();

        Field field = new Field(FieldType.TEXT, "cloneKey");
        field.setLabel("clone name");
        field.setValue(getValue("cloneKey", getKey(record), parameters));
        form.add(field);

        if(templatedCollection)
        {
            if(smart)
            {
                addParentField(form, parameters);
            }

            addDescendentFields(form);
        }
    }

    private void addParentField(Form form, Map parameters)
    {
        Field field;
        field = new Field(FieldType.TEXT, "parentKey");
        field.setLabel("extracted parent name");
        if (isInputSelected())
        {
            field.setValue(getKey(record) + " template");
        }
        else
        {
            field.setValue(getParameterValue(parameters, "parentKey"));
        }

        form.add(field);
    }

    private void addDescendentFields(final Form form)
    {
        final Map parameters = ActionContext.getContext().getParameters();
        TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
        templateNode.forEachDescendent(new TemplateNode.NodeHandler()
        {
            public boolean handle(TemplateNode node)
            {
                Record record = configurationTemplateManager.getRecord(node.getPath());
                String key = getKey(record);
                String nameField = KEY_FIELD_PREFIX + key;

                Field field = new Field(FieldType.CONTROLLING_CHECKBOX, CHECK_FIELD_PREFIX + key);
                field.addParameter(ControllingCheckboxFieldDescriptor.PARAM_INVERT, false);
                field.addParameter(ControllingCheckboxFieldDescriptor.PARAM_DEPENDENT_FIELDS, getDependentFields(nameField, node));
                field.setLabel("clone descendent '" + key + "'");
                if(parameters.containsKey(CHECK_FIELD_PREFIX + key))
                {
                    field.setValue("true");
                }
                form.add(field);

                field = new Field(FieldType.TEXT, nameField);
                field.setLabel("clone name");
                field.setValue(getValue(KEY_FIELD_PREFIX + key, key, parameters));
                form.add(field);

                return true;
            }
        }, true);
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

    private String getValue(String fieldName, String key, Map parameters)
    {
        if (isInputSelected())
        {
            return "clone of " + key;
        }
        else
        {
            return getParameterValue(parameters, fieldName);
        }
    }

    private String getKey(Record record)
    {
        return (String) record.get(mapType.getKeyProperty());
    }

    @Override
    protected void doAction()
    {
        if (smart)
        {
            configurationRefactoringManager.smartClone(parentPath, PathUtils.getBaseName(path), parentKey, keyMap);
        }
        else
        {
            configurationRefactoringManager.clone(parentPath, keyMap);
        }

        String newPath = PathUtils.getPath(parentPath, cloneKey);
        String templatePath = configurationTemplateManager.getTemplatePath(newPath);
        response = new ConfigurationResponse(newPath, templatePath);
        response.registerNewPathAdded(configurationTemplateManager, configurationSecurityManager);
        if (smart)
        {
            String newParent = PathUtils.getPath(parentPath, parentKey);
            String collapsedCollection = ToveUtils.getCollapsedCollection(newParent, configurationTemplateManager.getType(newParent), configurationSecurityManager);
            response.addAddedFile(new ConfigurationResponse.Addition(newParent, parentKey, configurationTemplateManager.getTemplatePath(newParent), collapsedCollection, ToveUtils.getIconCls(newParent, configurationTemplateManager), false, false));
            response.addRemovedPath(path);
        }
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
