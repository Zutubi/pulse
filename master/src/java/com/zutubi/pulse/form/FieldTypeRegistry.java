package com.zutubi.pulse.form;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.validator.Validator;

import java.util.List;

/**
 * <class-comment/>
 */
public interface FieldTypeRegistry
{
    /**
     * Retrieve the default field type associated with the defined class type.  For example, by default, an
     * java.lang.String field is associated with the TEXT field type.
     *
     * @param type
     *
     * @return the mapped field type, or null if no default field type support exists.
     */
    String getDefaultFieldType(Class type);

    /**
     * Returns true if the specified field type is valid.
     *
     * @param fieldType
     *
     * @return true if the specified field type is valid / supported, and false otherwise.
     */
    boolean supportsFieldType(String fieldType);

    /**
     * Register a new field type.
     *
     * @param fieldType defines the field type identifier.
     * @param squeezer defines the TypeSqueezer used by fields of this type.
     * @param validators defines the validators used by fields of this type.
     */
    void register(String fieldType, TypeSqueezer squeezer, List<Validator> validators);

    /**
     * Unregister an existing field type.
     *
     * @param fieldType is the field type identifier representing the field type to be unregistered.
     */
    void unregister(String fieldType);

    /**
     * Retrieve the type squeezer associated with the specified field type.
     *
     * @param fieldType is the field type being queried. This field type must be supported.
     *
     * @return a type squeezer.
     *
     * @throws IllegalArgumentException if the field type is not supported
     */
    TypeSqueezer getSqueezer(String fieldType);

    /**
     * Retrieve the validators associated with the specified field type.
     *
     * @param fieldType is the field type being queried. This field type must be supported.
     *
     * @return a list of validators.
     *
     * @throws IllegalArgumentException if the field type is not supported
     */
    List<Validator> getValidators(String fieldType);
}
