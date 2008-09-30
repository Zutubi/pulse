package com.zutubi.tove;

import com.zutubi.tove.type.record.Record;

/**
 *
 *
 */
public interface Descriptor extends Parameterised
{
    Object instantiate(String path, Record record);
}
