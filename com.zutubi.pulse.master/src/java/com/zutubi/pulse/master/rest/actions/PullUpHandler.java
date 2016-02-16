package com.zutubi.pulse.master.rest.actions;

import com.zutubi.pulse.master.rest.Validation;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.ui.model.ActionModel;
import com.zutubi.tove.ui.model.forms.DropdownFieldModel;
import com.zutubi.tove.ui.model.forms.FormModel;

import java.util.Map;

/**
 * Handles the pull up refactoring.
 */
public class PullUpHandler implements ActionHandler
{
    private static final String FIELD_ANCESTOR_KEY = "ancestorKey";

    private ConfigurationRefactoringManager configurationRefactoringManager;

    @Override
    public ActionModel getModel(String path, String variant)
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

        String ancestor = Validation.getRequiredString(FIELD_ANCESTOR_KEY, "ancestor", input);
        if (!configurationRefactoringManager.canPullUp(path, ancestor))
        {
            throw Validation.newFieldError(FIELD_ANCESTOR_KEY, "cannot pull up to ancestor '" + ancestor + "'");
        }

        return ancestor;
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
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
