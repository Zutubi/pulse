/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class Sort
{
    /**
     * A string comparator that delegates the comparison to the default
     * collator instance.
     *
     * @see Collator
     */
    public static class StringComparator implements Comparator<String>
    {
        private final Collator collator = Collator.getInstance();

        public int compare(String o1, String o2)
        {
            return collator.compare(o1, o2);
        }
    }

    /**
     * A comparator that delegates the comparison to an ordered set of
     * comparators.  If the first comparator returns a 0 comparison, then
     * the next comparator in the chain is consulted.  This continues until
     * the end of the chain is reached.  The first non zero response from
     * any of the comparators is returned. 
     *
     * @param <T> the type being compared.
     */
    public static class ChainComparator<T> implements Comparator<T>
    {
        private List<Comparator<T>> chain = new LinkedList<Comparator<T>>();

        public ChainComparator(Comparator<T>... chain)
        {
            this.chain.addAll(Arrays.asList(chain));
        }

        public int compare(T o1, T o2)
        {
            for (Comparator<T> comparator : chain)
            {
                int result = comparator.compare(o1, o2);
                if (result != 0)
                {
                    return result;
                }
            }
            return 0;
        }
    }

    /**
     * The inverse comparator inverts the result of a delegate
     * comparator.
     *
     * @param <T>   the type being compared.
     */
    public static class InverseComparator<T> implements Comparator<T>
    {
        private Comparator<T> delegate;

        public InverseComparator(Comparator<T> delegate)
        {
            this.delegate = delegate;
        }

        public int compare(T o1, T o2)
        {
            return -delegate.compare(o1, o2);
        }
    }

    /**
     * A comparator that sort alphabetically, but takes the Java package
     * notation <path>.<path>.<name> into consideration.  Strings are first
     * sort by package path, then by name within the package.  Names are not
     * compared with paths: where a name and subpath exist at the same level
     * the name is considered to come before the subpath regardless of the
     * alphabetical order of the name and subpath name.
     */
    public static class PackageComparator implements Comparator<String>
    {
        private final Collator collator = Collator.getInstance();

        public int compare(String p1, String p2)
        {
            String firstPath = "";
            String firstName = p1;
            String secondPath= "";
            String secondName= p2;

            int index = p1.lastIndexOf('.');
            if(index >= 0)
            {
                firstPath = p1.substring(0, index);
                if(index < p1.length() - 1)
                {
                    firstName = p1.substring(index + 1);
                }
            }

            index = p2.lastIndexOf('.');
            if(index >= 0)
            {
                secondPath = p2.substring(0, index);
                if(index < p2.length() - 1)
                {
                    secondName = p2.substring(index + 1);
                }
            }

            if(firstPath.equals(secondPath))
            {
                // Paths are identical, compare name
                return collator.compare(firstName, secondName);
            }
            else
            {
                // Paths differ, compare path
                return collator.compare(firstPath, secondPath);
            }
        }
    }
}
