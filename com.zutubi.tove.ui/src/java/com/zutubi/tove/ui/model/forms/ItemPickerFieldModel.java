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

import java.util.List;

/**
 * An item picker is a type of multi-select control that shows the selected set in one box, and a
 * list of options in another.
 */
public class ItemPickerFieldModel extends OptionFieldModel
{
    public ItemPickerFieldModel()
    {
        setType(FieldType.ITEM_PICKER);
    }

    public ItemPickerFieldModel(String name, String label, List list)
    {
        super(FieldType.ITEM_PICKER, name, label, list);
    }
}
