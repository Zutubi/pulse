package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
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
    private static final Messages I18N = Messages.getInstance(CloneAction.class);

    private static final String ACTION_SMART_CLONE = "smartclone";
    private static final String ACTION_CLONE = "clone";

    private static final String FIELD_CLONE_KEY = "cloneKey";
    private static final String FIELD_PARENT_KEY = "parentKey";

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
    private ClassificationManager classificationManager;

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
            throw new IllegalArgumentException(I18N.format("path.noParent", new Object[]{path}));
        }

        Type parentType = configurationTemplateManager.getType(parentPath);
        if(!(parentType instanceof MapType))
        {
            throw new IllegalArgumentException(I18N.format("path.notMapElement", new Object[]{path}));
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
        validateCloneKey(FIELD_CLONE_KEY, cloneKey, seenKeys, textProvider);

        keyMap = new HashMap<String, String>();
        keyMap.put(PathUtils.getBaseName(path), cloneKey);

        if(templatedCollection)
        {
            if(smart)
            {
                validateCloneKey(FIELD_PARENT_KEY, parentKey, seenKeys, textProvider);
            }

            getDescendants(keyMap, seenKeys, textProvider);
        }
    }

    private void validateCloneKey(String name, String value, Set<String> seenKeys, TextProvider textProvider)
    {
        if(!StringUtils.stringSet(value))
        {
            addFieldError(name, I18N.format("name.required"));
        }
        else
        {
            if(seenKeys.contains(value))
            {
                addFieldError(name, I18N.format("name.duplicate"));
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

    private Map<String, String> getDescendants(Map<String, String> selectedDescedents, Set<String> seenKeys, TextProvider textProvider)
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(CHECK_FIELD_PREFIX))
            {
                String descendantKey = name.substring(CHECK_FIELD_PREFIX.length());
                String value = getParameterValue(parameters, KEY_FIELD_PREFIX + descendantKey);

                if (value != null)
                {
                    validateCloneKey(KEY_FIELD_PREFIX + descendantKey, value, seenKeys, textProvider);
                    selectedDescedents.put(descendantKey, value);
                }
            }
        }

        return selectedDescedents;
    }

    @Override
    protected String getFormAction()
    {
        return smart ? ACTION_SMART_CLONE : ACTION_CLONE;
    }

    @Override
    protected void addFormFields(Form form)
    {
        Map parameters = ActionContext.getContext().getParameters();

        Field field = new Field(FieldType.TEXT, FIELD_CLONE_KEY);
        field.setLabel(I18N.format("cloneName.label"));
        field.setValue(getValue(FIELD_CLONE_KEY, getKey(record), parameters));
        form.add(field);

        if(templatedCollection)
        {
            if(smart)
            {
                addParentField(form, parameters);
            }

            addDescendantFields(form);
        }
    }

    private void addParentField(Form form, Map parameters)
    {
        Field field;
        field = new Field(FieldType.TEXT, FIELD_PARENT_KEY);
        field.setLabel(I18N.format("extractedParent.label"));
        if (isInputSelected())
        {
            field.setValue(I18N.format("extractedParent.default", new Object[]{getKey(record)}));
        }
        else
        {
            field.setValue(getParameterValue(parameters, FIELD_PARENT_KEY));
        }

        form.add(field);
    }

    private void addDescendantFields(final Form form)
    {
        final Map parameters = ActionContext.getContext().getParameters();
        TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
        templateNode.forEachDescendant(new TemplateNode.NodeHandler()
        {
            public boolean handle(TemplateNode node)
            {
                Record record = configurationTemplateManager.getRecord(node.getPath());
                String key = getKey(record);
                String nameField = KEY_FIELD_PREFIX + key;

                Field field = new Field(FieldType.CONTROLLING_CHECKBOX, CHECK_FIELD_PREFIX + key);
                field.addParameter(ControllingCheckboxFieldDescriptor.PARAM_CHECKED_FIELDS, getDependentFields(nameField, node));
                field.setLabel(I18N.format("cloneDescendant.label", new Object[]{key}));
                if(parameters.containsKey(CHECK_FIELD_PREFIX + key))
                {
                    field.setValue("true");
                }
                form.add(field);

                field = new Field(FieldType.TEXT, nameField);
                field.setLabel(I18N.format("cloneName.label"));
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
            return I18N.format("cloneName.default", new Object[]{key});
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
        response.registerNewPathAdded(configurationTemplateManager, configurationSecurityManager, classificationManager);
        if (smart)
        {
            String newParent = PathUtils.getPath(parentPath, parentKey);
            String collapsedCollection = ToveUtils.getCollapsedCollection(newParent, configurationTemplateManager.getType(newParent), configurationSecurityManager);
            response.addAddedFile(new ConfigurationResponse.Addition(newParent, parentKey, configurationTemplateManager.getTemplatePath(newParent), collapsedCollection, ToveUtils.getIconCls(newParent, classificationManager), false, false));
            response.addRemovedPath(path);
        }
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }

    public void setClassificationManager(ClassificationManager classificationManager)
    {
        this.classificationManager = classificationManager;
    }
}
