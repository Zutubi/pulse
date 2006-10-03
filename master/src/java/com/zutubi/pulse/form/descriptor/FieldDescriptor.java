package com.zutubi.pulse.form.descriptor;

import com.zutubi.pulse.form.ui.components.Component;

import java.util.Map;

/**
 * The field descriptor defines the meta data used to define a field in an HTML form.
 *
 *
 */
public interface FieldDescriptor extends Descriptor
{
    /**
     * If this field is a required field, then this method should return true. Required fields are checked
     * during validation to ensure that a value is provided.
     *
     * @return true if this field is required, false otherwise.
     */
    boolean isRequired();

    /**
     * Set the required status of this field.
     *
     * @param b
     */
    void setRequired(boolean b);

    /**
     * Get the raw class type of this field.  This is the type of object that the contents of this field will
     * be converted into.   
     *
     * @return
     */
    Class getType();

    /**
     * Get the name of this field.  The name of the field defines the name of the parameter returned by
     * the HTML form.
     *
     * @return the field name.
     */
    String getName();

    /**
     *
     * @return this fields FieldType
     */
    String getFieldType();

    /**
     * Set the field type.
     *
     * @param type
     */
    void setFieldType(String type);

    Map<String, Object> getParameters();
}
