package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.UnaryFunction;

/**
 * Static factory methods for creating {@link RecordUpgrader} instances.
 */
public class RecordUpgraders
{
    /**
     * Create a new upgrader that will add a simple property to records.
     *
     * @param name  the name of the property to add
     * @param value the default value for the property, which must be a simple
     *              value (a string or a string array)
     * @return the new upgrader
     * @throws IllegalArgumentException if the value given is not simple
     */
    public static RecordUpgrader newAddProperty(String name, Object value)
    {
        return new AddPropertyRecordUpgrader(name, value);
    }

    /**
     * Create a new upgrader that will delete a simple property from records.
     *
     * @param name the name of the property to delete, must be a simple
     *        property (not a nested record)
     * @return the new upgrader
     */
    public static RecordUpgrader newDeleteProperty(String name)
    {
        return new DeletePropertyRecordUpgrader(name);
    }

    /**
     * Create a new upgrader that will edit an existing simple property in
     * records.
     * <p/>
     * <b>Note</b> - this upgrader does not scrub inherited values in templated
     * scopes.  What this means in practice is that if the editing function can
     * result in a record having the same value for the property as its
     * template parent, then the resulting records will be invalid.  Editing
     * functions that always produce a different answer for different inputs
     * are safe.  Those that can produce the same output from different inputs
     * may not be.
     *
     * @param name   name of the property to edit
     * @param editFn function to edit the existing values, by returning
     *               corresponding new values.  This function should be able to
     *               handle a null input (no current value) and may produce a
     *               null output to indicate that any existing value should be
     *               removed.
     * @return the new upgrader
     */
    public static RecordUpgrader newEditProperty(String name, UnaryFunction<Object, Object> editFn)
    {
        return new EditPropertyRecordUpgrader(name, editFn);
    }

    /**
     * Create a new upgrader that will rename a simple property on records.
     *
     * @param oldName the name of the existing property which will be renamed
     *                - note that this must be a simple propery, not a nested
     *                record
     * @param newName the new name for the property, which must not clash with
     *                existing names
     * @return the new upgrader
     */
    public static RecordUpgrader newRenameProperty(String oldName, String newName)
    {
        return new RenamePropertyRecordUpgrader(oldName, newName);
    }

    /**
     * Create a new upgrader that will add a meta property to records.
     *
     * @param name  the name of the meta property to add
     * @param value the default value for the property
     * @return the new upgrader
     */
    public static RecordUpgrader newAddMetaProperty(String name, String value)
    {
        return new AddMetaPropertyRecordUpgrader(name, value);
    }

    /**
     * Create a new upgrader that will delete a meta property from records.
     *
     * @param name the name of the meta property to delete
     * @return the new upgrader
     */
    public static RecordUpgrader newDeleteMetaProperty(String name)
    {
        return new DeleteMetaPropertyRecordUpgrader(name);
    }
}
