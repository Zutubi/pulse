package com.zutubi.util;

import java.text.Collator;
import java.util.Comparator;

/**
 * <class-comment/>
 */
public class Sort
{
    public static class StringComparator implements Comparator<String>
    {
        private final Collator collator = Collator.getInstance();

        public int compare(String o1, String o2)
        {
            return collator.compare(o1, o2);
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
