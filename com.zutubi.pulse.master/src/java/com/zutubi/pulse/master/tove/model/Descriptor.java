package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.type.record.Record;
import com.zutubi.pulse.master.tove.model.Parameterised;

/**
 *
 *
 */
public interface Descriptor extends Parameterised
{
    Object instantiate(String path, Record record);
}
