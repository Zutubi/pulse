package com.zutubi.pulse.master.tove.handler;

import com.zutubi.tove.type.TypeProperty;

import java.util.List;

/**
 * An option provider is used to populate select fields (drop-downs,
 * multi-select lists, etc) in the UI.  Typically this interface is
 * implemented indirectly by sub classing {@link ListOptionProvider} or
 * {@link MapOptionProvider}.
 */
public interface OptionProvider
{
    /**
     * Returns an object representing the empty option, which will be
     * presented as an additional choice to the user when configuring a
     * template (in the single select case where the user is forced to
     * choose from the drop-down).
     *
     * @param instance   the current instance containing the field, which may
     *                   be null if a new object is being created
     * @param parentPath the path of the parent of the instance that the
     *                   field is defined on
     * @param property   type information for the property we are providing
     *                   options for.  Note that the property type may be a
     *                   collection: options should be based on the target
     *                   type of the property's type.
     * @return the empty option, or null if it is non-sensical to have such
     *         an option even in the templating case
     */
    Object getEmptyOption(Object instance, String parentPath, TypeProperty property);

    /**
     * Get the list of options to be presented to the user.  Each option is
     * an object that can be used to determine a string to display to the
     * user and a string for the UI to submit when the option is selected.
     * For simple cases, the option itself can be a string that serves both
     * purposes.
     *
     * @param instance   the current instance containing the field, which may
     *                   be null if a new object is being created
     * @param parentPath the path of the parent of the instance that the
     *                   field is defined on
     * @param property   type information for the property we are providing
     *                   options for.  Note that the property type may be a
     *                   collection: options should be based on the target
     *                   type of the property's type.
     * @return a list of the available options
     */
    List getOptions(Object instance, String parentPath, TypeProperty property);

    /**
     * Defines the key/property to be used to retrieve the option key from the
     * items in the collection.  The key is submitted back by the UI when the
     * user selects the option.
     *
     * @return the key / property name used to retrieve the option key from the
     * collection item, or null if the item itself is the key.
     */
    String getOptionKey();

    /**
     * Defines the key/property to be used to retrieve the option value from the
     * items in the collection.  The value is displayed to the user.
     *
     * @return the key / property name used to retrieve the option value from the 
     * collection item, or null if the item itself is the value.
     */
    String getOptionValue();
}
