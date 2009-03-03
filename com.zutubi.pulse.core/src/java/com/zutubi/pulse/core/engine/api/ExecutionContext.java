package com.zutubi.pulse.core.engine.api;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * Provides access to the current execution environment.  This environment
 * consists primarily of build properties, but also includes the working
 * directory and a stream for recording output.
 * <p/>
 * Note that properties are stored in distinct namespaces, to separate
 * internal and configurable properties.  See the NAMESPACE_* constants in
 * {@link com.zutubi.pulse.core.engine.api.BuildProperties} for available
 * namespaces, and the PROPERTY_* constants for property names.  When
 * looking up a value without specifying the namespace, the configurable
 * namespace is searched first, followed by the internal one.
 * <p/>
 * Note that the majority of properties are stored in string form, but
 * using various helper methods may be looked up as other simple types
 * such as booleans, numbers and files.
 *
 * @see com.zutubi.pulse.core.engine.api.BuildProperties
 */
public interface ExecutionContext
{
    /**
     * The current working directory, e.g. the bootstrap directory during SCM
     * checkout, the base directory during recipe execution.
     *
     * @return the file that defines the current working directory
     */
    File getWorkingDir();

    /**
     * The output stream to which feedback/logging can be written.  Data
     * written to this output stream will end up in the appropriate log file,
     * e.g. the recipe or build log.
     *
     * @return stream for recording feedback output may be null if no such
     *         stream is available in this context
     */
    OutputStream getOutputStream();

    /**
     * Retrieves the value of the given property from the given namespace as
     * a string.  If the given property does not exist or is not a string,
     * null is returned.
     *
     * @see #getString(String)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace the namespace to search
     * @param name      the property to retrieve
     * @return the property's string value, or null if it does not exist
     */
    String getString(String namespace, String name);

    /**
     * Searches namespaces in order for the given property, returning the
     * first value found.  If the given property does not exist or the value
     * is not a string, null is returned.
     *
     * @see #getString(String, String)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param name the property to retrieve
     * @return the property's string value, or null if it does not exist
     */
    String getString(String name);

    /**
     * Retrieves the value of the given property from the given namespace as
     * a boolean.  If the given property does not exist or cannot be
     * converted to a boolean, the default value is returned.
     *
     * @see #getBoolean(String, boolean)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace    the namespace to search
     * @param name         the property to retrieve
     * @param defaultValue default to return if no boolean value is found
     * @return the property's boolean value, or defaultValue if it does not
     *         exist
     */
    boolean getBoolean(String namespace, String name, boolean defaultValue);

    /**
     * Searches namespaces in order for the given property, returning the
     * first value found.  If the given property does not exist or the value
     * cannot be converted to a boolean, the default value is returned.
     *
     * @see #getBoolean(String, String, boolean)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param name         the property to retrieve
     * @param defaultValue default to return if no boolean value is found
     * @return the property's boolean value, or defaultValue if it does not
     *         exist
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Retrieves the value of the given property from the given namespace as
     * a long.  If the given property does not exist or cannot be converted
     * to a long, the default value is returned.
     *
     * @see #getLong(String, long)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace    the namespace to search
     * @param name         the property to retrieve
     * @param defaultValue default to return if no long value is found
     * @return the property's long value, or defaultValue if it does not
     *         exist
     */
    long getLong(String namespace, String name, long defaultValue);

    /**
     * Searches namespaces in order for the given property, returning the
     * first value found.  If the given property does not exist or the value
     * cannot be converted to a long, the default value is returned.
     *
     * @see #getLong(String, String, long)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param name         the property to retrieve
     * @param defaultValue default to return if no long value is found
     * @return the property's long value, or defaultValue if it does not
     *         exist
     */
    long getLong(String name, long defaultValue);

    /**
     * Retrieves the value of the given property from the given namespace as
     * a {@link java.io.File}.  If the given property does not exist or
     * cannot be converted to a file, null is returned.
     *
     * @see #getFile(String)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace the namespace to search
     * @param name      the property to retrieve
     * @return the property's File value, or null if it does not exist
     */
    File getFile(String namespace, String name);

    /**
     * Searches namespaces in order for the given property, returning the
     * first value found.  If the given property does not exist or the value
     * cannot be converted to a {@link java.io.File}, null is returned.
     *
     * @see #getFile(String, String)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param name the property to retrieve
     * @return the property's File value, or null if it does not exist
     */
    File getFile(String name);

    /**
     * Retrieves the value of the given property from the given namespace as
     * the given type.  If the given property does not exist or is not an
     * instance of the given type, null is returned.
     *
     * @see #getValue(String, Class)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace the namespace to search
     * @param name      the property to retrieve
     * @param type      the expected type of the property value
     * @return the property's value, or null if it does not exist or is not
     *         an instance of the given type
     */
    <T> T getValue(String namespace, String name, Class<T> type);

    /**
     * Searches namespaces in order for the given property, returning the
     * first value found.  If the given property does not exist or the value
     * is not an instance of the given type, null is returned.
     *
     * @see #getValue(String, String, Class)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param name the property to retrieve
     * @param type the expected type of the property value
     * @return the property's value, or null if it does not exist or is not
     *         an instance of the given type
     */
    <T> T getValue(String name, Class<T> type);

    /**
     * Adds the given reference to the given namespace.  The reference value
     * may later be looked up using the reference's name.  Any existing
     * reference of the same name will be replaced.
     *
     * @see #add(Reference)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace the namespace to add the reference to
     * @param reference the reference to add
     */
    void add(String namespace, Reference reference);

    /**
     * Adds the given reference to the user namespace.  The reference value
     * may later be looked up using the reference's name.  Any existing
     * reference of the same name will be replaced.
     *
     * @see #add(String, Reference)
     *
     * @param reference the reference to add
     */
    void add(Reference reference);

    /**
     * Adds the given resource property to the given namespace.  If the
     * property is set to resolve variables, its value will be resolved at
     * this time.  The value may later be looked up using the property's
     * name.  Any existing reference of the same name will be replaced.
     *
     * @see #add(ResourceProperty)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace        the namespace to add the proeprty to
     * @param resourceProperty the property to add
     */
    void add(String namespace, ResourceProperty resourceProperty);

    /**
     * Adds the given resource property to the user namespace.  If the
     * property is set to resolve variables, its value will be resolved at
     * this time.  The value may later be looked up using the property's
     * name.  Any existing reference of the same name will be replaced.
     *
     * @see #add(String, ResourceProperty)
     *
     * @param resourceProperty the property to add
     */
    void add(ResourceProperty resourceProperty);

    /**
     * Convenience method to add a property with the given name and value to
     * the given namespace.  The name and value are wrapped up into a
     * {@link PropertyConfiguration} and added.  Any existing reference of the same name
     * will be replaced.
     *
     * @see #addString(String, String)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace the namespace to add the property to
     * @param name      the property name
     * @param value     the property value
     */
    void addString(String namespace, String name, String value);

    /**
     * Convenience method to add a property with the given name and value to
     * the user namespace.  The name and value are wrapped up into a
     * {@link PropertyConfiguration} and added.  Any existing reference of the same name
     * will be replaced.
     *
     * @see #addString(String, String, String)
     *
     * @param name  the property name
     * @param value the property value
     */
    void addString(String name, String value);

    /**
     * Convenience method to add a property with the given name and value to
     * the given namespace.  The name and value are wrapped up into a
     * {@link com.zutubi.pulse.core.GenericReference} and added.  Any
     * existing reference of the same name will be replaced.
     *
     * @see #addValue(String, Object)
     * @see com.zutubi.pulse.core.engine.api.BuildProperties
     *
     * @param namespace the namespace to add the property to
     * @param name      the property name
     * @param value     the property value
     */
    void addValue(String namespace, String name, Object value);

    /**
     * Convenience method to add a property with the given name and value to
     * the user namespace.  The name and value are wrapped up into a
     * {@link com.zutubi.pulse.core.GenericReference} and added.  Any
     * existing reference of the same name will be replaced.
     *
     * @see #addValue(String, Object)
     *
     * @param name      the property name
     * @param value     the property value
     */
    void addValue(String name, Object value);

    /**
     * Resolves any references of the form ${&lt;name&gt;} in the given
     * string, returning the resolved form.  Each reference is replaced with
     * the property value converted to a string.  If no value is found for
     * &lt;name&gt;, the reference is left untouched.
     * <p/>
     * As the $ character is given special meaning, a literal $ must be
     * escaped with a backslash.  To include a literal backslashes, it must
     * be escaped with another backslash.  In the returned value the escaping
     * slashes are stripped leaving just the literal values.
     *
     * @param input the string containing references to resolve
     * @return the input string with all escaping and references resolved
     */
    String resolveReferences(String input);

    /**
     * In a single pass, resolves all references in the given string and
     * splits the string at space characters.  For the resolution rules, see
     * {@link #resolveReferences(String)}.
     * <p/>
     * The string is split at any occurence of one or more ASCII space
     * characters.  The space characters are discarded.  To include a literal
     * space character, escape it with a backslash or surround a portion of
     * the string including the space(s) with double quotes (").  As double
     * quotes have this special meaning, to include a literal double quote
     * character, escape it with a backslash.
     *
     * @param input the string containing references to resolve and split
     * @return the pieces of the input after resolution and splitting
     */
    List<String> splitAndResolveReferences(String input);
}
