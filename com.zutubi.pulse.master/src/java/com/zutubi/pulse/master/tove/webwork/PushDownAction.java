package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationRefactoringManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Action to prompt the user for the children that they would like to push down
 * a composite to.
 */
public class PushDownAction extends ToveFormActionSupport
{
    private static final Messages I18N = Messages.getInstance(PushDownAction.class);

    private static final String FIELD_NEW_PATH = "newPath";
    private static final String FIELD_CHILD_KEYS = "childKeys";

    /**
     * Path that we should return to after completion/cancellation (may be
     * supplied by the UI, if not default to path).
     */
    private String newPath;
    /**
     * Children to push the configuration down to.
     */
    private String[] childKeys;

    private ConfigurationRefactoringManager configurationRefactoringManager;

    public PushDownAction()
    {
        super(ConfigurationRefactoringManager.ACTION_PUSH_DOWN, "push down");
    }

    public void setNewPath(String newPath)
    {
        this.newPath = newPath;
    }

    public void setChildKeys(String[] childKeys)
    {
        this.childKeys = childKeys;
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
        if (!configurationRefactoringManager.canPushDown(path))
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
    protected void initialiseParameters()
    {
        List<String> children = configurationRefactoringManager.getPushDownChildren(path);
        childKeys = children.toArray(new String[children.size()]);
    }

    @Override
    protected void addFormFields(Form form)
    {
        Field field = new Field(FieldType.ITEM_PICKER, FIELD_CHILD_KEYS);
        field.setLabel(I18N.format(FIELD_CHILD_KEYS + ".label"));
        field.addParameter(OptionFieldDescriptor.PARAMETER_LIST, configurationRefactoringManager.getPushDownChildren(path));
        field.setValue(childKeys);
        form.add(field);

        field = new Field(FieldType.HIDDEN, FIELD_NEW_PATH);
        field.setValue(newPath);
        form.add(field);
    }

    @Override
    protected void validateForm()
    {
        Map params = ActionContext.getContext().getParameters();
        this.childKeys = (String[]) params.get(FIELD_CHILD_KEYS);
        if (childKeys == null || childKeys.length == 0)
        {
            addFieldError(FIELD_CHILD_KEYS, I18N.format(FIELD_CHILD_KEYS + ".required"));
        }
        else
        {
            for (String childKey: childKeys)
            {
                if (!configurationRefactoringManager.canPushDown(path, childKey))
                {
                    addFieldError(FIELD_CHILD_KEYS, I18N.format(FIELD_CHILD_KEYS + ".invalid", new Object[]{childKey}));
                }
            }
        }
    }

    @Override
    protected void doAction()
    {
        configurationRefactoringManager.pushDown(path, new HashSet<String>(Arrays.asList(childKeys)));
        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
