package com.zutubi.pulse.prototype.record;

import java.util.Map;

/**
 * <class comment/>
 */
public interface Record extends Map<String, Object>
{
    Object get(Object name);

    String getSymbolicName();
}
