package com.zutubi.pulse.form.descriptor;

import com.zutubi.pulse.form.ui.components.FormComponent;

import java.util.List;
import java.util.Map;

/**
 * The form descriptor provides the meta data required to work with a visual form.  It defines things like the
 * forms fields and its actions.
 *
 *
 */
public interface FormDescriptor extends Descriptor
{
    /**
     * Get the underlying type definition of for this form descriptor.
     *
     */
    Class getType();

    /**
     * Retrieve the list of this forms field descriptors.
     *
     * @return a list of field descriptors.
     */
    List<FieldDescriptor> getFieldDescriptors();

    /**
     * Retrieve the named field descriptor.
     *
     * @param name identifying the field descriptor.
     *
     * @return a field descriptor, or null if it does not exist.
     */
    //Q: should we throw an exception if the name does not match a field?
    FieldDescriptor getFieldDescriptor(String name);

    /**
     * Get the list of field names, in the order in which they should be rendered. If this method
     * returns null, then the order of the fields is undefined.
     *
     * This method should return the full list of field names.  Any missing field names will result in
     * that field not being appended to the end in some arbitrary order.
     *
     * @return a list of field names.
     */
    String[] getFieldOrder();

    void setFieldOrder(String[] order);

    /**
     * Get the list of action descriptors defined for this form. The action descriptors define the
     * submits available for this form.
     *
     * @return a list of action descriptors.
     */
    List<ActionDescriptor> getActionDescriptors();

    /**
     * Set the list of action descriptors defined for this form.
     * 
     * @param actions
     */
    void setActionDescriptors(List<ActionDescriptor> actions);

    /**
     * The parameter map defines a set of 'extra' parameters for this form.
     *
     * @return a map of key:value pairs.
     */
    Map<String, Object> getParameters();
}
