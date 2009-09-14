package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.UnaryFunction;

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
    private UnaryFunction<Object, Object> editFn;

    /**
     * @param name   the name of the property to add
     * @param editFn function to turn the existing value into the edited value.
     *               This function should be able to handle a null input (no
     *               current value) and may produced a null output to indicate
     *               that any existing value should be removed.
     */
    public EditPropertyRecordUpgrader(String name, UnaryFunction<Object, Object> editFn)
    {
        this.name = name;
        this.editFn = editFn;
    }

    public void upgrade(String path, MutableRecord record)
    {
        Object value = record.get(name);
        value = editFn.process(value);
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
