package com.zutubi.prototype;

import com.zutubi.prototype.type.TypeProperty;

import java.util.Collection;

/**
 * 
 *
 */
public interface OptionProvider
{
    /**
     * Get the list of options to be presented to the user.  Each option is
     * an entry mapping the displayed string to the value corresponding to
     * that selection.
     *
     * @param instance   the current instance containing the field, which may
     *                   be null if a new object is being created
     * @param parentPath the path of the parent of the instance that the
     *                   field is defined on
     * @param property   type information for the property we are providing
     *                   options for.  Note that the property type may be a
     *                   collection: options should be based on the target
     *                   type of the property's type.
     * @return a collection of the available options
     */
    Collection getOptions(Object instance, String parentPath, TypeProperty property);

    /**
     * Defines the key/property to be used to retrieve the option key from the
     * items in the collection.
     *
     * @return the key / property name used to retrieve the option key from the
     * collection item.
     */
    String getOptionKey();

    /**
     * Defines the key/property to be used to retrieve the option value from the
     * items in the collection.
     *
     * @return the key / property name used to retrieve the option value from the 
     * collection item.
     */
    String getOptionValue();
}
