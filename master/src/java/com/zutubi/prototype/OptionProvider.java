package com.zutubi.prototype;

import com.zutubi.prototype.type.TypeProperty;

import java.util.Map;
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
     * @param path     the concrete path of the property that we are
     *                 providing options for
     * @param property type information for the property we are providing
     *                 options for
     * @return a collection of the available options
     */
    Collection getOptions(String path, TypeProperty property);

    String getOptionKey();

    String getOptionValue();
}
