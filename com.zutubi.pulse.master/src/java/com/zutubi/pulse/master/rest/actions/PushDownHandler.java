package com.zutubi.pulse.master.rest.actions;

import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.ActionModel;
import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.pulse.master.rest.model.forms.ItemPickerFieldModel;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.api.ActionResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handler for the push down refactoring.
 */
public class PushDownHandler implements ActionHandler
{
    private static final String FIELD_CHILD_KEYS = "childKeys";

    private ConfigurationRefactoringManager configurationRefactoringManager;

    @Override
    public ActionModel getModel(String path)
    {
        if (!configurationRefactoringManager.canPushDown(path))
        {
            throw new IllegalArgumentException("Cannot push down path '" + path + "'");
        }

        ActionModel model = new ActionModel(ConfigurationRefactoringManager.ACTION_PUSH_DOWN, "push down", null, true);
        FormModel form = new FormModel();
        form.addField(new ItemPickerFieldModel(FIELD_CHILD_KEYS, "to children", configurationRefactoringManager.getPushDownChildren(path)));
        model.setForm(form);
        return model;
    }

    @Override
    public ActionResult doAction(String path, Map<String, Object> input)
    {
        Object childKeysValue = input.get(FIELD_CHILD_KEYS);
        if (childKeysValue == null || !(childKeysValue instanceof List) || ((List) childKeysValue).size() == 0)
        {
            ValidationException ex = new ValidationException();
            ex.addFieldError(FIELD_CHILD_KEYS, "at least one child is required");
            throw ex;
        }

        List childKeys = (List) childKeysValue;
        Set<String> childSet = new HashSet<>();
        for (Object childKey: childKeys)
        {
            if (!configurationRefactoringManager.canPushDown(path, childKey.toString()))
            {
                ValidationException ex = new ValidationException();
                ex.addFieldError(FIELD_CHILD_KEYS, "cannot push down to child '" + childKey + "'");
                throw ex;
            }

            childSet.add(childKey.toString());
        }

        configurationRefactoringManager.pushDown(path, childSet);

        return new ActionResult(ActionResult.Status.SUCCESS, "pushed down");
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
