package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

import java.util.Arrays;
import java.util.Map;

/**
 * Action to gather sibling keys and options before introducing a parent
 * template.
 */
public class IntroduceParentAction extends ToveFormActionSupport
{
    private static final Messages I18N = Messages.getInstance(IntroduceParentAction.class);

    private static final String FIELD_PARENT_KEY = "parentKey";
    private static final String FIELD_PULL_UP = "pullUp";

    private Record record;
    private String parentPath;
    private MapType mapType;
    private String parentKey;
    private boolean pullUp;

    private ConfigurationRefactoringManager configurationRefactoringManager;
    private ClassificationManager classificationManager;

    public IntroduceParentAction()
    {
        super("introduceparent", "ok");
    }

    public void setParentKey(String parentKey)
    {
        this.parentKey = parentKey;
    }

    public void setPullUp(boolean pullUp)
    {
        this.pullUp = pullUp;
    }

    @Override
    public void doCancel()
    {
        response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
    }

    @Override
    protected void validatePath()
    {
        parentPath = PathUtils.getParentPath(path);
        if (parentPath == null)
        {
            throw new IllegalArgumentException(I18N.format("path.noParent", path));
        }

        Type parentType = configurationTemplateManager.getType(parentPath);
        if(!(parentType instanceof MapType))
        {
            throw new IllegalArgumentException(I18N.format("path.notMapElement", path));
        }

        if (!configurationTemplateManager.isTemplatedCollection(parentPath))
        {
            throw new IllegalArgumentException(I18N.format("path.notTemplated", path));
        }

        if (configurationTemplateManager.getTemplateNode(path).getParent() == null)
        {
            throw new IllegalArgumentException(I18N.format("path.root", path));
        }
        
        mapType = (MapType) parentType;
        type = parentType.getTargetType();
        record = configurationTemplateManager.getRecord(path);
    }

    @Override
    protected void validateForm()
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());
        validateParentKey(FIELD_PARENT_KEY, parentKey, textProvider);
    }

    private void validateParentKey(String name, String value, TextProvider textProvider)
    {
        if (!StringUtils.stringSet(value))
        {
            addFieldError(name, I18N.format("name.required"));
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
    }

    @Override
    protected void addFormFields(Form form)
    {
        Map parameters = ActionContext.getContext().getParameters();

        Field parentKeyField = new Field(FieldType.TEXT, FIELD_PARENT_KEY);
        parentKeyField.setLabel(I18N.format("parentKey.label"));

        Field pullUpField = new Field(FieldType.CHECKBOX, FIELD_PULL_UP);
        pullUpField.setLabel(I18N.format("pullUp.label"));
        if (isInputSelected())
        {
            parentKeyField.setValue(I18N.format("parentKey.default", getKey(record)));
            pullUpField.setValue("false");
        }
        else
        {
            parentKeyField.setValue(getParameterValue(parameters, FIELD_PARENT_KEY));
            if (parameters.containsKey(FIELD_PULL_UP))
            {
                pullUpField.setValue("true");
            }
            else
            {
                pullUpField.setValue("false");
            }
        }
        
        form.add(parentKeyField);
        form.add(pullUpField);
    }

    private String getKey(Record record)
    {
        return (String) record.get(mapType.getKeyProperty());
    }

    @Override
    protected void doAction()
    {
        configurationRefactoringManager.introduceParentTemplate(parentPath, Arrays.asList(PathUtils.getBaseName(path)), parentKey, pullUp);

        String newPath = PathUtils.getPath(parentPath, parentKey);
        String templatePath = configurationTemplateManager.getTemplatePath(newPath);
        response = new ConfigurationResponse(newPath, templatePath);
        response.registerNewPathAdded(configurationTemplateManager, configurationSecurityManager, classificationManager);
        response.addRemovedPath(path);
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
