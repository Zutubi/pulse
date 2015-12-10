package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.pulse.master.tove.model.ActionLink;

import java.util.Map;

/**
 * Models an action that can be invoked on a configuration.  Note there may be multiple actions
 * with the same 'action', but they will have different 'variant's.
 *
 * FIXME kendo should replace ActionLink
 */
public class ActionModel
{
    private String action;
    private String label;
    private String variant;
    private boolean inputRequired;
    private FormModel form;
    private Map<String, Object> formDefaults;

    public ActionModel(ActionLink actionLink, boolean inputRequired)
    {
        this(actionLink.getAction(), actionLink.getLabel(), actionLink.getArgument(), inputRequired);
    }

    public ActionModel(String action, String label, String variant, boolean inputRequired)
    {
        this.action = action;
        this.label = label;
        this.variant = variant;
        this.inputRequired = inputRequired;
    }

    public String getAction()
    {
        return action;
    }

    public String getLabel()
    {
        return label;
    }

    public String getVariant()
    {
        return variant;
    }

    public boolean isInputRequired()
    {
        return inputRequired;
    }

    public FormModel getForm()
    {
        return form;
    }

    public void setForm(FormModel form)
    {
        this.form = form;
    }

    public Map<String, Object> getFormDefaults()
    {
        return formDefaults;
    }

    public void setFormDefaults(Map<String, Object> formDefaults)
    {
        this.formDefaults = formDefaults;
    }
}
