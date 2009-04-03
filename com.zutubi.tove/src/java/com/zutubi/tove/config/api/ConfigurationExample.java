package com.zutubi.tove.config.api;

/**
 * Holds information about an example usage of a configuration object.
 */
public class ConfigurationExample
{
    private String element;
    private Configuration configuration;

    /**
     * Creates a new example.
     *
     * @param element       name to use for the root element when rendering of
     *                      this example as XML
     * @param configuration pre-configured instance that shows typical usage of
     *                      a configuration type
     */
    public ConfigurationExample(String element, Configuration configuration)
    {
        this.element = element;
        this.configuration = configuration;
    }

    /**
     * @return name to use for the root element when rendering of this example
     *         as XML
     */
    public String getElement()
    {
        return element;
    }

    /**
     * @return pre-configured instance that shows typical usage of a
     *         configuration type
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }
}
