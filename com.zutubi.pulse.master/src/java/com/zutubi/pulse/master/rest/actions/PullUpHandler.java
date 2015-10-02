package com.zutubi.pulse.master.rest.actions;

import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.ActionModel;
import com.zutubi.pulse.master.rest.model.forms.DropdownFieldModel;
import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.api.ActionResult;

import java.util.Map;

/**
 * Handles the pull up refactoring.
 */
public class PullUpHandler implements ActionHandler
{
    private static final String FIELD_ANCESTOR_KEY = "ancestorKey";

    private ConfigurationRefactoringManager configurationRefactoringManager;

    @Override
    public ActionModel getModel(String path)
    {
        if (!configurationRefactoringManager.canPullUp(path))
        {
            throw new IllegalArgumentException("Cannot pull up path '" + path + "'");
        }

        ActionModel model = new ActionModel(ConfigurationRefactoringManager.ACTION_PULL_UP, "pull up", null, true);
        FormModel form = new FormModel();
        form.addField(new DropdownFieldModel(FIELD_ANCESTOR_KEY, "to ancestor", configurationRefactoringManager.getPullUpAncestors(path)));
        model.setForm(form);
        return model;
    }

    private String validate(String path, Map<String, Object> input)
    {
        if (!configurationRefactoringManager.canPullUp(path))
        {
            throw new IllegalArgumentException("Cannot pull up path '" + path + "'");
        }

        Object ancestorKey = input.get(FIELD_ANCESTOR_KEY);
        if (ancestorKey == null || !(ancestorKey instanceof String) || ((String) ancestorKey).length() == 0)
        {
            ValidationException exception = new ValidationException();
            exception.addFieldError(FIELD_ANCESTOR_KEY, "ancestor is required");
            throw exception;
        }

        String ancestor = (String) ancestorKey;
        if (!configurationRefactoringManager.canPullUp(path, ancestor))
        {
            ValidationException exception = new ValidationException();
            exception.addFieldError(FIELD_ANCESTOR_KEY, "cannot pull up to ancestor '" + ancestor + "'");
            throw exception;
        }

        return ancestor;
    }

    @Override
    public ActionResult doAction(String path, Map<String, Object> input)
    {
        String ancestorKey = validate(path, input);
        configurationRefactoringManager.pullUp(path, ancestorKey);
        return new ActionResult(ActionResult.Status.SUCCESS, "pulled up");
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
