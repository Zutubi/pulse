package com.zutubi.pulse.upgrade.tasks;

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
    public static RecordUpgrader newAddProperty(String name, String value)
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
}
