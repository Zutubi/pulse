package com.zutubi.i18n.bundle;

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A factory interface for creating resource bundle instances.
 */
public interface ResourceBundleFactory
{
    /**
     * Create a resource bundle instance from the input stream.
     *
     * @param input the input stream contains the data to be used to populate
     * the resource bundle.
     * @param locale the locale of the data contained by the input stream.
     * @return a new resource bundle instance
     *
     * @throws IOException if there are any problems loading the resource bundle
     * from the input stream.
     */
    public ResourceBundle loadBundle(InputStream input, Locale locale) throws IOException;

    /**
     * Given the resource name, this method will expand that resource name to
     * a list of comparable resource names based on the specified locale.
     *
     * @param name the base bundle name
     * @param locale the locale to be used when expanding the base name.
     * 
     * @return the list of expanded bundle names.
     */
    public List<String> expand(String name, Locale locale);

}
