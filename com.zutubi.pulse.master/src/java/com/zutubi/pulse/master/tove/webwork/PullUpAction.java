package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationRefactoringManager;

/**
 * Action to prompt the user for the ancestor that they would like to pull up
 * a composite to.
 */
public class PullUpAction extends ToveFormActionSupport
{
    private static final Messages I18N = Messages.getInstance(PullUpAction.class);
    
    private static final String FIELD_NEW_PATH = "newPath";
    private static final String FIELD_ANCESTOR_KEY = "ancestorKey";

    private String newPath;
    private String ancestorKey;

    private ConfigurationRefactoringManager configurationRefactoringManager;

    public PullUpAction()
    {
        super(ConfigurationRefactoringManager.ACTION_PULL_UP, "pull up");
    }

    public void setNewPath(String newPath)
    {
        this.newPath = newPath;
    }

    public void setAncestorKey(String ancestorKey)
    {
        this.ancestorKey = ancestorKey;
    }

    @Override
    public void doCancel()
    {
        if (newPath == null)
        {
            newPath = path;
        }

        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
    }

    @Override
    protected void validatePath()
    {
        if (!configurationRefactoringManager.canPullUp(path))
        {
            throw new IllegalArgumentException(I18N.format("path.invalid", new Object[]{path}));
        }

        if (newPath == null)
        {
            newPath = path;
        }

        type = configurationTemplateManager.getType(path);
    }

    @Override
    protected void addFormFields(Form form)
    {
        Field field = new Field(FieldType.DROPDOWN, FIELD_ANCESTOR_KEY);
        field.setLabel(I18N.format(FIELD_ANCESTOR_KEY + ".label"));
        field.addParameter(OptionFieldDescriptor.PARAMETER_LIST, configurationRefactoringManager.getPullUpAncestors(path));
        field.setValue(ancestorKey);
        form.add(field);

        field = new Field(FieldType.HIDDEN, FIELD_NEW_PATH);
        field.setValue(newPath);
        form.add(field);
    }

    @Override
    protected void validateForm()
    {
        if (!TextUtils.stringSet(ancestorKey))
        {
            addFieldError(FIELD_ANCESTOR_KEY, I18N.format(FIELD_ANCESTOR_KEY + ".required"));
        }
        else
        {
            if (!configurationRefactoringManager.canPullUp(path, ancestorKey))
            {
                addFieldError(FIELD_ANCESTOR_KEY, I18N.format(FIELD_ANCESTOR_KEY + ".invalid"));
            }
        }
    }

    @Override
    protected void doAction()
    {
        configurationRefactoringManager.pullUp(path, ancestorKey);
        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}