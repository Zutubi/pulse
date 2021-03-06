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

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.tove.type.record.MutableRecord;

/**
 * Edits an existing property of a record, using a specified function.  The
 * property value must be simple (a string or string array).
 * <p/>
 * <b>Note</b> - this upgrader does not scrub inherited values in templated
 * scopes.  What this means in practice is that if the editing function can
 * result in a record having the same value for the property as its template
 * parent, then the resulting records will be invalid.  Editing functions that
 * always produce a different answer for different inputs are safe.  Those that
 * can produce the same output from different inputs may not be.
 */
class EditPropertyRecordUpgrader implements RecordUpgrader
{
    private String name;
    private Function<Object, Object> editFn;

    /**
     * @param name   the name of the property to add
     * @param editFn function to turn the existing value into the edited value.
     *               This function should be able to handle a null input (no
     *               current value) and may produced a null output to indicate
     *               that any existing value should be removed.
     */
    public EditPropertyRecordUpgrader(String name, Function<Object, Object> editFn)
    {
        this.name = name;
        this.editFn = editFn;
    }

    public void upgrade(String path, MutableRecord record)
    {
        Object value = record.get(name);
        value = editFn.apply(value);
        if (value == null)
        {
            record.remove(name);
        }
        else
        {
            record.put(name, value);
        }
    }
}
