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

import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.ui.model.forms.FormModel;

import java.util.Map;

/**
 * Models an action that can be invoked on a configuration.  Note there may be multiple actions
 * with the same 'action', but they will have different 'variant's.
 */
public class ActionModel
{
    private String action;
    private String label;
    private String variant;
    private boolean inputRequired;
    private FormModel form;
    private Map<String, Object> formDefaults;
    private TypeDocs docs;

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

    public TypeDocs getDocs()
    {
        return docs;
    }

    public void setDocs(TypeDocs docs)
    {
        this.docs = docs;
    }
}
