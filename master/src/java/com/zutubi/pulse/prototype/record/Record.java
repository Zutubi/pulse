package com.zutubi.pulse.prototype.record;

import java.util.Map;

/**
 * <class comment/>
 */
public interface Record extends Map<String, String>
{
    String get(Object name);

    String getSymbolicName();
}
