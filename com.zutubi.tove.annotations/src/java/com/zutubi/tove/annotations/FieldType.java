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

package com.zutubi.tove.annotations;

/**
 * Constants for field types.
 */
public interface FieldType
{
    String CHECKBOX = "checkbox";
    String COMBOBOX = "combobox";
    String CONTROLLING_CHECKBOX = "controlling-checkbox";
    String CONTROLLING_SELECT = "controlling-select";
    String DROPDOWN = "dropdown";
    String FILE = "file";
    /**
     * A field used to carry an internal value.
     */
    String HIDDEN = "hidden";
    String ITEM_PICKER = "itempicker";
    /**
     * A text field where the value is not echoed.
     */
    String PASSWORD = "password";
    String SUBMIT = "submit";
    String STRING_LIST = "stringlist";
    /**
     * Text field type, represents a plain string value.
     */
    String TEXT = "text";
    String TEXTAREA = "textarea";
}
