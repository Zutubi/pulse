package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.model.ItemPickerFieldDescriptor;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
     * Path that we should return to after cancellation (may be supplied by
     * the UI, if not default to path).  After completion we always go to the
     * parent path.
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
        if (!StringUtils.stringSet(newPath))
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
            throw new IllegalArgumentException(I18N.format("path.invalid", path));
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
        field.addParameter(ItemPickerFieldDescriptor.PARAMETER_SUPPRESS_DEFAULT, true);
        field.setValue(childKeys);
        form.add(field);

        field = new Field(FieldType.HIDDEN, FIELD_NEW_PATH);
        field.setValue(newPath);
        form.add(field);
    }

    @Override
    protected void validateForm()
    {
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
                    addFieldError(FIELD_CHILD_KEYS, I18N.format(FIELD_CHILD_KEYS + ".invalid", childKey));
                }
            }
        }
    }

    @Override
    protected void doAction()
    {
        String originalDisplayName = ToveUtils.getDisplayName(path, configurationTemplateManager);

        configurationRefactoringManager.pushDown(path, new HashSet<String>(Arrays.asList(childKeys)));

        String parentPath = PathUtils.getParentPath(path);
        boolean collectionElement = (configurationTemplateManager.getType(parentPath) instanceof CollectionType);
        newPath = collectionElement ? parentPath : path;
        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
        response.setStatus(new ConfigurationResponse.Status(ConfigurationResponse.Status.Type.SUCCESS, I18N.format("pushed.down")));

        if (collectionElement)
        {
            response.addRemovedPath(path);
        }
        else
        {
            String newDisplayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
            if(!newDisplayName.equals(originalDisplayName))
            {
                response.addRenamedPath(new ConfigurationResponse.Rename(path, path, newDisplayName, null));
            }
        }
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
