package com.zutubi.pulse.bootstrap;

import java.util.LinkedList;
import java.util.List;

/**
 * This simple list extension allows us to define beans within the spring context
 * that represent simple lists that can be reused and queried.
 */
public class SpringListBean extends LinkedList<Object>
{
    public void setList(List<Object> l)
    {
        for (Object o : l)
        {
            add(o);
        }
    }
}
