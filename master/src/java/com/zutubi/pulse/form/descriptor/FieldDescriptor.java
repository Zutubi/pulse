package com.zutubi.pulse.form.descriptor;

import com.zutubi.pulse.form.ui.components.Component;

import java.util.Map;

/**
 * <class-comment/>
 */
public interface FieldDescriptor extends Descriptor
{
    boolean isRequired();

    void setRequired(boolean b);

    Class getType();

    String getName();

    String getFieldType();

    Component createField();

    Map<String, Object> getParameters();
}
