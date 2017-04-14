/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.ui.model.forms.FormModel;

import java.util.HashMap;
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
    private WizardTypeFilter filter;
    private Map<String, Object> parameters;

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

    public WizardTypeFilter getFilter()
    {
        return filter;
    }

    public void setFilter(WizardTypeFilter filter)
    {
        this.filter = filter;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void addParameter(String name, Object value)
    {
        if (parameters == null)
        {
            parameters = new HashMap<>();
        }

        parameters.put(name, value);
    }
}
