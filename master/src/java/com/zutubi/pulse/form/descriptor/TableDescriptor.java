package com.zutubi.pulse.form.descriptor;

import java.util.List;

/**
 */
public interface TableDescriptor extends Descriptor
{
    List<ColumnDescriptor> getColumnDescriptors();
}
