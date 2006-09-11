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

    Class getType();

    /**
     * Get the name of this field.  The name of the field defines the name of the parameter returned by
     * the HTML form.
     *
     * @return the field name.
     */
    String getName();

    /**
     * The field type defines the data type and validation rules that are applied to the field. See the FieldType and
     * FieldTypeRegistry for more information on FieldTypes.
     *
     * @return this fields FieldType
     */
    String getFieldType();

    Map<String, Object> getParameters();

    Component createField();
}
