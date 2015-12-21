package com.zutubi.pulse.master.rest.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.tove.config.docs.TypeDocs;

import java.util.Map;

/**
 * A wizard step that has a custom form (rather than one based on a defined type).
 */
@JsonTypeName("custom")
public class CustomWizardStepModel extends WizardStepModel
{
    private FormModel form;
    private Map<String, Object> formDefaults;
    private TypeDocs docs;

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

    public TypeDocs getDocs()
    {
        return docs;
    }

    public void setDocs(TypeDocs docs)
    {
        this.docs = docs;
    }
}
