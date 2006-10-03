package com.zutubi.pulse.form.persist;

import com.zutubi.pulse.form.persist.PersistenceDescriptor;

/**
 * <class-comment/>
 */
public interface DescriptorFactory
{
    PersistenceDescriptor createDescriptor(Class type);
}
