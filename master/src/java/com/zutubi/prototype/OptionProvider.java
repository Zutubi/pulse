package com.zutubi.prototype;

import java.util.List;

/**
 * 
 *
 */
public interface OptionProvider
{
    /**
     * Get the list of options to be presented to the user
     *
     * @return a list of the available options.
     */
    List<String> getOptions();
}
