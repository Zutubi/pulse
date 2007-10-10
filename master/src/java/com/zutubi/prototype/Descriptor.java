package com.zutubi.prototype;

import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public interface Descriptor extends Parameterised
{
    Object instantiate(String path, Record record);
}
