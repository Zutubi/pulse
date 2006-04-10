package com.zutubi.pulse.util;

import java.text.Collator;
import java.util.Comparator;

/**
 * <class-comment/>
 */
public class Sort
{
    public static class StringComparator implements Comparator
    {
        private final Collator collator = Collator.getInstance();

        public int compare(Object o1, Object o2)
        {
            return collator.compare(o1, o2);
        }
    }
}
