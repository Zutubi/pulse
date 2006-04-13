/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.util;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class ListUtils<T>
{
    public List<T> filter(List<T> l, Predicate<T> p)
    {
        List<T> result = new LinkedList<T>();
        for(T t: l)
        {
            if(p.satisfied(t))
            {
                result.add(t);
            }
        }

        return result;
    }
}
