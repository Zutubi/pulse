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

package com.zutubi.tove.ui.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Models a checkbox that controls the enabled/disabled state of other fields in a form.
 */
public class ControllingCheckboxFieldModel extends FieldModel
{
    private String[] checkedFields;
    private String[] uncheckedFields;

    public ControllingCheckboxFieldModel()
    {
        this(null, null);
    }

    public ControllingCheckboxFieldModel(String name, String label)
    {
        super(FieldType.CONTROLLING_CHECKBOX, name, label);
    }

    public String[] getCheckedFields()
    {
        return checkedFields;
    }

    public void setCheckedFields(String[] checkedFields)
    {
        this.checkedFields = checkedFields;
    }

    public String[] getUncheckedFields()
    {
        return uncheckedFields;
    }

    public void setUncheckedFields(String[] uncheckedFields)
    {
        this.uncheckedFields = uncheckedFields;
    }
}
