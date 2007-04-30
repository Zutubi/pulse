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
     * @param instance the current instance containing the field, which may
     *                 be null if a new object is being created
     * @param path     the concrete path of the property that we are
     *                 providing options for
     * @param property type information for the property we are providing
     *                 options for.  Note that the property type may be a
     *                 collection: options should be based on the target
     *                 type of the property's type.
     * @return a collection of the available options
     */
    Collection getOptions(Object instance, String path, TypeProperty property);

    String getOptionKey();

    String getOptionValue();
}
