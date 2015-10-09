package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.rest.model.forms.FormModel;

import java.util.Map;

/**
 * A wizard step that has a custom form (rather than one based on a defined type).
 */
public class CustomWizardStepModel extends WizardStepModel
{
    private FormModel form;
    private Map<String, Object> formDefaults;

    public CustomWizardStepModel(String label, String key, FormModel form)
    {
        super(label, key);
        this.form = form;
    }

    public FormModel getForm()
    {
        return form;
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
