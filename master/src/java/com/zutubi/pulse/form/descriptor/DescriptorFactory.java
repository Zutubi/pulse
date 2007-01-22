package com.zutubi.pulse.form.descriptor;

import java.util.List;

/**
 * <class-comment/>
 */
public interface DescriptorFactory
{
    FormDescriptor createFormDescriptor(Class type);

    void setDecorators(List<DescriptorDecorator> decorators);

    TableDescriptor createTableDescriptor(Class type);
}
